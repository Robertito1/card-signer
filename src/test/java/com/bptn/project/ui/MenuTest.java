package com.bptn.project.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bptn.project.export.CardExporter;
import com.bptn.project.io.InputProvider;
import com.bptn.project.model.Card;
import com.bptn.project.model.Signature;
import com.bptn.project.model.template.CardTemplate;
import com.bptn.project.model.template.BasicCardTemplate;
import com.bptn.project.model.template.TemplateCatalog;
import com.bptn.project.render.AsciiCardRenderer;
import com.bptn.project.repository.FileCardRepository;
import com.bptn.project.repository.InMemoryCardRepository;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class MenuTest {

    /** Feeds queued answers to the menu; returns null (EOF) when exhausted. */
    private static class ScriptedInput implements InputProvider {
        private final Queue<String> lines;

        ScriptedInput(String... script) {
            lines = new ArrayDeque<>(Arrays.asList(script));
        }

        @Override
        public String readLine(String prompt) {
            return lines.poll();
        }
    }

    @TempDir
    Path temp;

    private InMemoryCardRepository repository;
    private CardExporter exporter;
    private ByteArrayOutputStream output;

    @BeforeEach
    public void setUp() {
        repository = new InMemoryCardRepository();
        exporter = new CardExporter(temp.resolve("exports"),
            new AsciiCardRenderer());
        output = new ByteArrayOutputStream();
    }

    private String run(String... script) {
        Menu menu = new Menu(repository, new AsciiCardRenderer(),
            exporter, new ScriptedInput(script), new PrintStream(output),
            TemplateCatalog.all());
        menu.run();
        return output.toString();
    }

    private Card cardWithSignature() {
        Card card = new Card("Alice", 30, "Birthday", new BasicCardTemplate());
        card.addSignature(new Signature("Bob", "Have a great year"));
        repository.save(card);
        return card;
    }

    @Test
    public void exitsOnNine() {
        String out = run("9");
        assertTrue(out.contains("Create a new card"));
        assertTrue(out.contains("Goodbye!"));
    }

    @Test
    public void exitsOnEof() {
        String out = run();
        assertTrue(out.contains("Goodbye!"));
    }

    @Test
    public void invalidMenuChoicesReportErrors() {
        String out = run("abc", "0", "10", "9");
        assertEquals(3, out.split("Error:").length - 1);
        assertTrue(out.contains("Goodbye!"));
    }

    @Test
    public void createCardFlowStoresCard() {
        String out = run("1", "1", "Alice", "30", "Birthday", "9");
        assertTrue(out.contains("Card created for Alice."));
        assertEquals(1, repository.findAll().size());
        assertEquals("Classic Birthday",
            repository.findAll().get(0).getTemplate().getName());
    }

    @Test
    public void createCardRePromptsOnBadInput() {
        String out = run("1", "9", "2", "Bad!name", "Alice", "abc", "30",
            "x".repeat(40), "Birthday", "9");
        assertTrue(out.contains("Card created for Alice."));
        assertEquals("Balloon Party", repository.findAll().get(0).getTemplate().getName());
    }

    @Test
    public void createCardCancelledOnEof() {
        String out = run("1");
        assertTrue(out.contains("Error: Input closed."));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void listShowsEmptyMessageAndCards() {
        assertTrue(run("2", "9").contains("No cards yet"));
        cardWithSignature();
        String out = run("2", "9");
        assertTrue(out.contains("Alice"));
        assertTrue(out.contains("1 signature(s)"));
    }

    @Test
    public void previewRendersCard() {
        cardWithSignature();
        String out = run("3", "1", "9");
        assertTrue(out.contains("WITH BEST WISHES"));
        assertTrue(out.contains("To: Alice"));
        assertTrue(out.contains("- Bob: Have a great year"));
    }

    @Test
    public void previewWithNoCardsAndBadSelection() {
        assertTrue(run("3", "9").contains("No cards yet"));
        cardWithSignature();
        String out = run("3", "5", "1", "9");
        assertTrue(out.contains("between 1 and 1"));
        assertTrue(out.contains("To: Alice"));
    }

    @Test
    public void signCardAddsSignature() {
        cardWithSignature();
        String out = run("4", "1", "Carol", "Many happy returns", "9");
        assertTrue(out.contains("Signature added for Alice."));
        assertEquals(2, repository.findAll().get(0).getSignatures().size());
    }

    @Test
    public void signCardRePromptsOnBadInput() {
        cardWithSignature();
        run("4", "1", "", "Bad!9", "Carol", "", "  ", "ok wish", "9");
        assertEquals("Carol", repository.findAll().get(0).getSignature(1).getSignerName());
        assertEquals("ok wish", repository.findAll().get(0).getSignature(1).getWish());
    }

    @Test
    public void signCancelledWhenNoCards() {
        assertTrue(run("4", "9").contains("No cards yet"));
    }

    @Test
    public void cardActionsExitEarlyWhenNoCards() {
        String out = run("3", "4", "5", "6", "7", "8", "9");
        assertEquals(6, out.split("No cards yet").length - 1);
    }

    @Test
    public void editSignatureUpdatesWish() {
        cardWithSignature();
        String out = run("5", "1", "1", "A better wish", "9");
        assertTrue(out.contains("Signature updated."));
        assertEquals("A better wish",
            repository.findAll().get(0).getSignature(0).getWish());
    }

    @Test
    public void editSignatureWithNoSignatures() {
        repository.save(new Card("Alice", 30, "Birthday", new BasicCardTemplate()));
        assertTrue(run("5", "1", "9").contains("no signatures yet"));
    }

    @Test
    public void editSignatureRePromptsOnBadIndex() {
        cardWithSignature();
        run("5", "1", "9", "1", "fixed", "9");
        assertEquals("fixed", repository.findAll().get(0).getSignature(0).getWish());
    }

    @Test
    public void removeSignatureDeletesIt() {
        cardWithSignature();
        String out = run("6", "1", "1", "9");
        assertTrue(out.contains("Signature removed."));
        assertTrue(repository.findAll().get(0).getSignatures().isEmpty());
    }

    @Test
    public void removeSignatureWithNoSignatures() {
        repository.save(new Card("Alice", 30, "Birthday", new BasicCardTemplate()));
        assertTrue(run("6", "1", "9").contains("no signatures yet"));
    }

    @Test
    public void deleteCardConfirmed() {
        cardWithSignature();
        String out = run("7", "1", "y", "9");
        assertTrue(out.contains("Card deleted."));
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void deleteCardCancelled() {
        cardWithSignature();
        String out = run("7", "1", "n", "9");
        assertTrue(out.contains("Cancelled."));
        assertFalse(repository.findAll().isEmpty());
    }

    @Test
    public void deleteCardRePromptsOnBadConfirm() {
        cardWithSignature();
        run("7", "1", "maybe", "yes", "9");
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    public void exportWritesFile() throws Exception {
        cardWithSignature();
        String out = run("8", "1", "9");
        assertTrue(out.contains("Exported to"));
        Path dir = temp.resolve("exports");
        assertTrue(Files.list(dir).findAny().isPresent());
    }

    @Test
    public void malformedCardFileReportsErrorAndKeepsRunning() throws Exception {
        Path dir = temp.resolve("cards");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("bad.card"), "this line has no separator");
        Menu menu = new Menu(new FileCardRepository(dir), new AsciiCardRenderer(),
            exporter, new ScriptedInput("2", "9"), new PrintStream(output),
            TemplateCatalog.all());
        menu.run();
        String out = output.toString();
        assertTrue(out.contains("Error: Malformed card file"));
        assertTrue(out.contains("Goodbye!"));
    }

    @Test
    public void constructorRejectsNulls() {
        CardTemplate t = TemplateCatalog.get(1);
        AsciiCardRenderer r = new AsciiCardRenderer();
        InputProvider in = new ScriptedInput("9");
        PrintStream ps = new PrintStream(output);
        assertThrows(NullPointerException.class,
            () -> new Menu(null, r, exporter, in, ps, java.util.List.of(t)));
    }
}
