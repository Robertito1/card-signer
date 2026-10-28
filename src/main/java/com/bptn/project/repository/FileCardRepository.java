package com.bptn.project.repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.bptn.project.model.Card;
import com.bptn.project.model.Signature;
import com.bptn.project.model.template.TemplateCatalog;

/**
 * Stores each card as a small line-based text file, one file per card, in a
 * directory. Format:
 *
 * <pre>
 * id=&lt;uuid&gt;
 * template=&lt;template key&gt;
 * recipient=&lt;escaped name&gt;
 * age=&lt;int&gt;
 * occasion=&lt;escaped occasion&gt;
 * sig=&lt;escaped signer&gt;|&lt;escaped wish&gt;
 * ...
 * </pre>
 *
 * In stored values, '\' escapes the next character so '|' and '\'
 * itself can be stored safely.
 */
public class FileCardRepository implements CardRepository {

    private static final String EXTENSION = ".card";

    private final Path directory;

    public FileCardRepository(Path directory) {
        this.directory = directory;
    }

    @Override
    public void save(Card card) {
        try {
            Files.createDirectories(directory);
            Files.write(pathFor(card.getId()), toLines(card));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save card " + card.getId(), e);
        }
    }

    @Override
    public List<Card> findAll() {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            List<Card> cards = new ArrayList<>();
            files.filter(f -> f.getFileName().toString().endsWith(EXTENSION))
                .forEach(f -> cards.add(load(f)));
            cards.sort(Comparator.comparing(c -> c.getRecipientName().toLowerCase()));
            return cards;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list cards in " + directory, e);
        }
    }

    @Override
    public Optional<Card> findById(String id) {
        Path file = pathFor(id);
        return Files.isRegularFile(file) ? Optional.of(load(file)) : Optional.empty();
    }

    @Override
    public boolean deleteById(String id) {
        try {
            return Files.deleteIfExists(pathFor(id));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete card " + id, e);
        }
    }

    private Path pathFor(String id) {
        return directory.resolve(id + EXTENSION);
    }

    private List<String> toLines(Card card) {
        List<String> lines = new ArrayList<>();
        lines.add("id=" + card.getId());
        lines.add("template=" + TemplateCatalog.keyOf(card.getTemplate()));
        lines.add("recipient=" + escape(card.getRecipientName()));
        lines.add("age=" + card.getAge());
        lines.add("occasion=" + escape(card.getOccasion()));
        for (Signature s : card.getSignatures()) {
            lines.add("sig=" + escape(s.getSignerName()) + "|" + escape(s.getWish()));
        }
        return lines;
    }

    private Card load(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read card file " + file, e);
        }
        String id = null;
        String templateKey = null;
        String recipient = null;
        String occasion = "Birthday";
        int age = 0;
        List<Signature> signatures = new ArrayList<>();
        try {
            for (String line : lines) {
                int eq = line.indexOf('=');
                if (eq < 0) {
                    throw new IllegalArgumentException("Malformed line: " + line);
                }
                String key = line.substring(0, eq);
                String value = line.substring(eq + 1);
                switch (key) {
                    case "id" -> id = unescape(value);
                    case "template" -> templateKey = value;
                    case "recipient" -> recipient = unescape(value);
                    case "age" -> age = Integer.parseInt(value);
                    case "occasion" -> occasion = unescape(value);
                    case "sig" -> signatures.add(parseSignature(value));
                    default -> { /* ignore unknown keys for forward compatibility */ }
                }
            }
            Card card = new Card(
                require(id, "id", file),
                require(recipient, "recipient", file),
                age,
                occasion,
                TemplateCatalog.byKey(require(templateKey, "template", file)));
            signatures.forEach(card::addSignature);
            return card;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Malformed card file: " + file, e);
        }
    }

    private String require(String value, String field, Path file) {
        if (value == null) {
            throw new IllegalArgumentException("Missing " + field + " in " + file);
        }
        return value;
    }

    private Signature parseSignature(String value) {
        List<String> parts = splitEscaped(value);
        if (parts.size() < 2) {
            throw new IllegalArgumentException("Malformed signature: " + value);
        }
        String wish = String.join("|", parts.subList(1, parts.size()));
        return new Signature(parts.get(0), wish);
    }

    private static String escape(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' || c == '|' || c == '\n' || c == '=') {
                out.append('\\');
            }
            out.append(c == '\n' ? 'n' : c);
        }
        return out.toString();
    }

    private static String unescape(String value) {
        List<String> parts = splitEscaped(value);
        return parts.isEmpty() ? "" : parts.get(0);
    }

    /**
     * Splits on unescaped '|', decoding '\n' to a newline and other '\x'
     * sequences back to 'x'. A trailing '\' is kept literally.
     */
    private static List<String> splitEscaped(String value) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                current.append(next == 'n' ? '\n' : next);
            } else if (c == '|') {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        return parts;
    }
}
