package com.bptn.project.ui;

import com.bptn.project.exception.ValidationException;
import com.bptn.project.export.CardExporter;
import com.bptn.project.io.InputProvider;
import com.bptn.project.model.Card;
import com.bptn.project.model.Signature;
import com.bptn.project.model.template.CardTemplate;
import com.bptn.project.render.CardRenderer;
import com.bptn.project.repository.CardRepository;
import com.bptn.project.util.InputValidator;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Drives the console menu. All input goes through the injected InputProvider
 * and every answer is validated via InputValidator, with re-prompting on
 * bad input.
 */
public class Menu {

    private static final int EXIT_CHOICE = 9;

    private final CardRepository repository;
    private final CardRenderer renderer;
    private final CardExporter exporter;
    private final InputProvider input;
    private final PrintStream out;
    private final List<CardTemplate> templates;

    public Menu(
        CardRepository repository,
        CardRenderer renderer,
        CardExporter exporter,
        InputProvider input,
        PrintStream out,
        List<CardTemplate> templates) {
        this.repository = Objects.requireNonNull(repository);
        this.renderer = Objects.requireNonNull(renderer);
        this.exporter = Objects.requireNonNull(exporter);
        this.input = Objects.requireNonNull(input);
        this.out = Objects.requireNonNull(out);
        this.templates = List.copyOf(templates);
    }

    // Menu entry point, this method gets called at the start of the application
    public void run() {
        out.println("=== Local Complementary/Birthday Card & Message Signer ===");
        boolean running = true;
        while (running) {
            printMenu();
            String raw = input.readLine("Choose an option: ");
            if (raw == null) {
                break;
            }
            try {
                running = dispatch(InputValidator.parseChoice(raw, EXIT_CHOICE));
            } catch (ValidationException | IllegalStateException | UncheckedIOException e) {
                out.println("Error: " + e.getMessage());
            }
        }
        out.println("Goodbye!");
    }

    private void printMenu() {
        out.println();
        out.println("1. Create a new card");
        out.println("2. List cards");
        out.println("3. Preview a card");
        out.println("4. Sign a card");
        out.println("5. Edit a signature");
        out.println("6. Remove a signature");
        out.println("7. Delete a card");
        out.println("8. Export a card to .txt");
        out.println("9. Exit");
    }

    private boolean dispatch(int choice) {
        switch (choice) {
            case 1 -> createCard();
            case 2 -> listCards();
            case 3 -> previewCard();
            case 4 -> signCard();
            case 5 -> editSignature();
            case 6 -> removeSignature();
            case 7 -> deleteCard();
            case 8 -> exportCard();
            case EXIT_CHOICE -> {
                return false;
            }
            default -> throw new ValidationException("Unknown option: " + choice);
        }
        return true;
    }

    // Enables the feature of card creation   
    private void createCard() {
    	Card card;
        out.println("-- Choose a template --");
        for (int i = 0; i < templates.size(); i++) {
            out.println((i + 1) + ". " + templates.get(i).getName());
        }
        Integer age = null;
        int choice = ask("Template number: ",
            raw -> InputValidator.parseChoice(raw, templates.size()));
        String name = ask("Recipient name: ", InputValidator::requireName);
        String occasion = ask("Occasion: ",
                raw -> InputValidator.requireText(raw, "Occasion", InputValidator.MAX_OCCASION_LENGTH));
        if(occasion.toLowerCase().contains("birthday")) {
            age = ask("Turning age: ", InputValidator::parseAge);
        }
        if (age != null) {
            card = new Card(name, age, occasion, templates.get(choice - 1));
        } else {
            card = new Card(name, occasion, templates.get(choice- 1));
        }
        repository.save(card);
        out.println("Card created for " + card.getRecipientName() + ".");
    }

    //Uses the describe method below to list cards on the console
    private void listCards() {
        List<Card> cards = repository.findAll();
        if (cards.isEmpty()) {
            out.println("No cards yet. Create one first.");
            return;
        }
        for (int i = 0; i < cards.size(); i++) {
            out.println((i + 1) + ". " + describe(cards.get(i)));
        }
    }

    //Renders card to the console  
    private void previewCard() {
        Card card = pickCard();
        if (card != null) {
            out.println();
            out.print(renderer.render(card));
        }
    }

    //Enables adding a Signature using the Card object method
    private void signCard() {
        Card card = pickCard();
        if (card == null) {
            return;
        }
        String signer = ask("Your name: ", InputValidator::requireName);
        String wish = ask("Your wish: ",
            raw -> InputValidator.requireText(raw, "Wish", InputValidator.MAX_WISH_LENGTH));
        card.addSignature(new Signature(signer, wish));
        repository.save(card);
        out.println("Signature added for " + card.getRecipientName() + ".");
    }

    //Enables Signature update using the Card object method
    private void editSignature() {
        Card card = pickCard();
        if (card == null) {
            return;
        }
        int index = pickSignature(card);
        if (index < 0) {
            return;
        }
        String wish = ask("New wish: ",
            raw -> InputValidator.requireText(raw, "Wish", InputValidator.MAX_WISH_LENGTH));
        card.updateSignature(index, wish);
        repository.save(card);
        out.println("Signature updated.");
    }

    //Enables Signature deletion using the Card object method
    private void removeSignature() {
        Card card = pickCard();
        if (card == null) {
            return;
        }
        int index = pickSignature(card);
        if (index < 0) {
            return;
        }
        card.removeSignature(index);
        repository.save(card);
        out.println("Signature removed.");
    }

    //Uses the repository to delete card
    private void deleteCard() {
        Card card = pickCard();
        if (card == null) {
            return;
        }
        boolean confirmed = ask(
            "Really delete the card for " + card.getRecipientName() + "? (y/n): ",
            InputValidator::parseYesNo);
        if (confirmed) {
            repository.deleteById(card.getId());
            out.println("Card deleted.");
        } else {
            out.println("Cancelled.");
        }
    }

    //Uses the exporter to export card to a .txt file
    private void exportCard() {
        Card card = pickCard();
        if (card == null) {
            return;
        }
        Path file = exporter.export(card);
        out.println("Exported to " + file.toAbsolutePath());
    }

    /** Prints the card list and returns the picked card, or null if none. */
    private Card pickCard() {
        List<Card> cards = repository.findAll();
        if (cards.isEmpty()) {
            out.println("No cards yet. Create one first.");
            return null;
        }
        for (int i = 0; i < cards.size(); i++) {
            out.println((i + 1) + ". " + describe(cards.get(i)));
        }
        int choice = ask("Select card: ",
            raw -> InputValidator.parseChoice(raw, cards.size()));
        return cards.get(choice - 1);
    }

    /** Prints a card's signatures and returns the 0-based index picked, or -1. */
    private int pickSignature(Card card) {
        List<Signature> signatures = card.getSignatures();
        if (signatures.isEmpty()) {
            out.println("This card has no signatures yet.");
            return -1;
        }
        for (int i = 0; i < signatures.size(); i++) {
            out.println((i + 1) + ". " + signatures.get(i));
        }
        return ask("Select signature: ",
            raw -> InputValidator.parseChoice(raw, signatures.size())) - 1;
    }

    // Used for listing the cards on the console
    private String describe(Card card) {
    	if(card.getAge() != null) {
    		return card.getRecipientName() + " - Turning " + card.getAge()
    		+ " "+ card.getOccasion() + ", "
            + card.getSignatures().size() + " signature(s), "
            + card.getTemplate().getName();
        }
        return card.getRecipientName() + " - For their " + card.getOccasion() + 
        	", " + card.getSignatures().size() + " signature(s), "
            + card.getTemplate().getName();
    }

    /**
     * Prompts until {@code parser} accepts the input, re-prompting on
     * ValidationException. Throws ValidationException if input ends (EOF),
     * which returns the user to the menu.
     */
    private <T> T ask(String prompt, Function<String, T> parser) {
        while (true) {
            String raw = input.readLine(prompt);
            if (raw == null) {
                throw new ValidationException("Input closed.");
            }
            try {
                return parser.apply(raw);
            } catch (ValidationException e) {
                out.println("  " + e.getMessage());
            }
        }
    }
}

