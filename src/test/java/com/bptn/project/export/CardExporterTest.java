package com.bptn.project.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bptn.project.model.Card;
import com.bptn.project.model.template.BirthdayCardTemplate;
import com.bptn.project.render.AsciiCardRenderer;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CardExporterTest {

    @TempDir
    Path temp;

    private Path exportDir;
    private CardExporter exporter;
    private final AsciiCardRenderer renderer = new AsciiCardRenderer();

    @BeforeEach
    public void setUp() {
        exportDir = temp.resolve("exports");
        exporter = new CardExporter(exportDir, renderer);
    }

    @Test
    public void exportWritesRenderedCard() throws Exception {
        Card card = new Card("Alice", 30, "Birthday", new BirthdayCardTemplate());
        Path file = exporter.export(card);
        assertTrue(Files.isRegularFile(file));
        assertEquals(renderer.render(card), Files.readString(file));
        assertTrue(file.getFileName().toString().matches("Alice-[0-9a-f]{8}\\.txt"));
    }

    @Test
    public void filenameIsSanitized() {
        Card card = new Card("Mary Jane", 40, "Birthday", new BirthdayCardTemplate());
        Path file = exporter.export(card);
        assertTrue(file.getFileName().toString().startsWith("Mary-Jane-"));
    }

    @Test
    public void filenameFallsBackToCard() {
        Card card = new Card("Мария", 25, "Birthday", new BirthdayCardTemplate());
        Path file = exporter.export(card);
        assertTrue(file.getFileName().toString().startsWith("card-"));
    }

    @Test
    public void exportFailsWhenDirectoryIsAFile() throws Exception {
        CardExporter bad = new CardExporter(Files.createFile(temp.resolve("blocked")), renderer);
        assertThrows(UncheckedIOException.class,
            () -> bad.export(new Card("Alice", 30, "Birthday", new BirthdayCardTemplate())));
    }

    @Test
    public void constructorRejectsNulls() {
        assertThrows(NullPointerException.class, () -> new CardExporter(null, renderer));
        assertThrows(NullPointerException.class, () -> new CardExporter(exportDir, null));
    }
}
