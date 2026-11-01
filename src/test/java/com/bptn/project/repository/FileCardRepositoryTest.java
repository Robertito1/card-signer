package com.bptn.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bptn.project.model.Card;
import com.bptn.project.model.Signature;
import com.bptn.project.model.template.BirthdayCardTemplate;
import com.bptn.project.model.template.BasicCardTemplate;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileCardRepositoryTest {

    @TempDir
    Path temp;

    private Path dir;
    private FileCardRepository repo;

    @BeforeEach
    public void setUp() {
        dir = temp.resolve("cards");
        repo = new FileCardRepository(dir);
    }

    @Test
    public void findAllIsEmptyWhenDirectoryMissing() {
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    public void saveThenReloadRoundTrips() {
        Card card = new Card("Alice", 30, "Birthday", new BirthdayCardTemplate());
        card.addSignature(new Signature("Bob", "Great year ahead"));
        card.addSignature(new Signature("Ann", "Pipes | and \\ slashes = ok"));
        repo.save(card);

        Optional<Card> found = repo.findById(card.getId());
        assertTrue(found.isPresent());
        Card loaded = found.get();
        assertEquals(card.getId(), loaded.getId());
        assertEquals("Alice", loaded.getRecipientName());
        assertEquals(30, loaded.getAge());
        assertEquals("Birthday", loaded.getOccasion());
        assertEquals(card.getTemplate().getClass(), loaded.getTemplate().getClass());
        assertEquals(card.getSignatures(), loaded.getSignatures());
    }

    @Test
    public void findAllReturnsSortedCards() {
        repo.save(new Card("Zed", 1, "Birthday", new BasicCardTemplate()));
        repo.save(new Card("amy", 2, "Birthday", new BirthdayCardTemplate()));
        List<Card> all = repo.findAll();
        assertEquals(2, all.size());
        assertEquals("amy", all.get(0).getRecipientName());
        assertEquals("Zed", all.get(1).getRecipientName());
    }

    @Test
    public void saveOverwritesSameId() {
        Card card = new Card("Alice", 30, "Birthday", new BirthdayCardTemplate());
        repo.save(card);
        card.addSignature(new Signature("Bob", "hi"));
        repo.save(card);
        assertEquals(1, repo.findAll().size());
        assertEquals(1, repo.findById(card.getId()).get().getSignatures().size());
    }

    @Test
    public void findByIdMissingIsEmpty() {
        assertTrue(repo.findById("nope").isEmpty());
    }

    @Test
    public void deleteByIdRemovesFile() {
        Card card = new Card("Alice", 30, "Birthday", new BirthdayCardTemplate());
        repo.save(card);
        assertTrue(repo.deleteById(card.getId()));
        assertFalse(repo.deleteById(card.getId()));
        assertTrue(repo.findAll().isEmpty());
    }

    @Test
    public void ignoresNonCardFiles() throws Exception {
        repo.save(new Card("Alice", 30, "Birthday", new BirthdayCardTemplate()));
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("notes.txt"), "hello");
        assertEquals(1, repo.findAll().size());
    }

    @Test
    public void malformedFileThrowsIllegalState() throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("bad.card"), "this line has no separator");
        assertThrows(IllegalStateException.class, () -> repo.findAll());
    }

    @Test
    public void missingTemplateKeyThrowsIllegalState() throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("x.card"),
            "id=abc\nrecipient=Alice\nage=30\noccasion=Birthday\n");
        assertThrows(IllegalStateException.class, () -> repo.findAll());
    }

    @Test
    public void malformedSignatureLineThrowsIllegalState() throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("x.card"),
            "id=abc\ntemplate=BirthdayCardTemplate\nrecipient=Alice\nage=30\n"
                + "occasion=Birthday\nsig=no-delimiter-here\n");
        assertThrows(IllegalStateException.class, () -> repo.findAll());
    }

    @Test
    public void saveFailsWhenDirectoryIsAFile() throws Exception {
        Path notADir = Files.createFile(temp.resolve("blocked"));
        FileCardRepository badRepo = new FileCardRepository(notADir);
        assertThrows(UncheckedIOException.class,
            () -> badRepo.save(new Card("Alice", 30, "Birthday", new BirthdayCardTemplate())));
    }

    @Test
    public void loadFailsWhenCardPathIsUnreadable() throws Exception {
        Files.createDirectories(dir.resolve("fake.card"));
        assertThrows(UncheckedIOException.class, () -> repo.findAll());
    }

    @Test
    public void deleteFailsWhenCardPathIsNonEmptyDirectory() throws Exception {
        Path fake = dir.resolve("card-id.card");
        Files.createDirectories(fake);
        Files.writeString(fake.resolve("inner"), "x");
        assertThrows(UncheckedIOException.class, () -> repo.deleteById("card-id"));
    }

    @Test
    public void escapedNewlineLoadsAsSpace() throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("x.card"),
            "id=abc\ntemplate=BirthdayCardTemplate\nrecipient=Alice\nage=30\n"
                + "occasion=Birthday\nsig=Bob|line one\\nline two\n");
        assertEquals("line one line two", repo.findAll().get(0).getSignatures().get(0).getWish());
    }

    @Test
    public void unknownKeysAreIgnored() throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("x.card"),
            "id=abc\ntemplate=BirthdayCardTemplate\nrecipient=Alice\nage=30\n"
                + "occasion=Birthday\nfuturefield=zzz\nsig=Bob|hi there\n");
        Card loaded = repo.findAll().get(0);
        assertEquals("Alice", loaded.getRecipientName());
        assertEquals(List.of(new Signature("Bob", "hi there")), loaded.getSignatures());
    }
}
