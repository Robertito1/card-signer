package com.bptn.project.export;


import com.bptn.project.model.Card;
import com.bptn.project.render.CardRenderer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Writes a rendered card to a .txt file in an export directory.
 */
public class CardExporter {

    private final Path directory;
    private final CardRenderer renderer;

    public CardExporter(Path directory, CardRenderer renderer) {
        this.directory = Objects.requireNonNull(directory);
        this.renderer = Objects.requireNonNull(renderer);
    }

    /**
     * Exports the card and returns the path of the file that was written.
     */
    public Path export(Card card) {
        String base = card.getRecipientName()
            .replaceAll("[^A-Za-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
        if (base.isEmpty()) {
            base = "card";
        }
        String shortId = card.getId().substring(0, Math.min(8, card.getId().length()));
        Path file = directory.resolve(base + "-" + shortId + ".txt");
        try {
            Files.createDirectories(directory);
            Files.writeString(file, renderer.render(card));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not export card to " + file, e);
        }
        return file;
    }
}
