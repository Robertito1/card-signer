package com.bptn.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bptn.project.exception.ValidationException;
import com.bptn.project.model.template.BirthdayCardTemplate;
import com.bptn.project.model.template.CardTemplate;
import org.junit.jupiter.api.Test;

public class CardTest {

    private final CardTemplate template = new BirthdayCardTemplate();

    @Test
    public void constructorGeneratesIdAndValidates() {
        Card card = new Card("Alice", 30, "Birthday", template);
        assertNotNull(card.getId());
        assertEquals("Alice", card.getRecipientName());
        assertEquals(30, card.getAge());
        assertEquals("Birthday", card.getOccasion());
        assertSame(template, card.getTemplate());
        assertTrue(card.getSignatures().isEmpty());
    }

    @Test
    public void explicitIdIsKept() {
        Card card = new Card("fixed-id", "Alice", 30, "Birthday", template);
        assertEquals("fixed-id", card.getId());
    }

    @Test
    public void rejectsInvalidFields() {
        assertThrows(ValidationException.class, () -> new Card("  ", 30, "Birthday", template));
        assertThrows(ValidationException.class, () -> new Card("Alice", -1, "Birthday", template));
        assertThrows(ValidationException.class, () -> new Card("Alice", 200, "Birthday", template));
        assertThrows(ValidationException.class, () -> new Card("Alice", 30, " ", template));
        assertThrows(NullPointerException.class, () -> new Card("Alice", 30, "Birthday", null));
        assertThrows(NullPointerException.class, () -> new Card(null, "Alice", 30, "Birthday", template));
    }

    @Test
    public void settersValidate() {
        Card card = new Card("Alice", 30, "Birthday", template);
        card.setRecipientName("Bob");
        card.setAge(31);
        card.setOccasion("Anniversary");
        assertEquals("Bob", card.getRecipientName());
        assertEquals(31, card.getAge());
        assertEquals("Anniversary", card.getOccasion());
        assertThrows(ValidationException.class, () -> card.setRecipientName("X1"));
        assertThrows(ValidationException.class, () -> card.setAge(-5));
        assertThrows(ValidationException.class, () -> card.setOccasion(""));
    }

    @Test
    public void signaturesCanBeAddedAndListed() {
        Card card = new Card("Alice", 30, "Birthday", template);
        Signature s = new Signature("Bob", "hi");
        card.addSignature(s);
        assertEquals(1, card.getSignatures().size());
        assertSame(s, card.getSignature(0));
    }

    @Test
    public void signatureListIsUnmodifiable() {
        Card card = new Card("Alice", 30, "Birthday", template);
        assertThrows(UnsupportedOperationException.class,
            () -> card.getSignatures().add(new Signature("Bob", "hi")));
        assertThrows(NullPointerException.class, () -> card.addSignature(null));
    }

    @Test
    public void signatureIndexIsBoundsChecked() {
        Card card = new Card("Alice", 30, "Birthday", template);
        assertThrows(ValidationException.class, () -> card.getSignature(0));
        card.addSignature(new Signature("Bob", "hi"));
        assertThrows(ValidationException.class, () -> card.getSignature(1));
        assertThrows(ValidationException.class, () -> card.getSignature(-1));
        assertThrows(ValidationException.class, () -> card.updateSignature(5, "x"));
        assertThrows(ValidationException.class, () -> card.removeSignature(-1));
    }

    @Test
    public void updateAndRemoveSignature() {
        Card card = new Card("Alice", 30, "Birthday", template);
        card.addSignature(new Signature("Bob", "old"));
        card.addSignature(new Signature("Ann", "second"));
        card.updateSignature(0, "new");
        assertEquals("new", card.getSignature(0).getWish());
        Signature removed = card.removeSignature(0);
        assertEquals("Bob", removed.getSignerName());
        assertEquals("Ann", card.getSignature(0).getSignerName());
    }

    @Test
    public void equalityBasedOnId() {
        Card a = new Card("same", "Alice", 30, "Birthday", template);
        Card b = new Card("same", "Bob", 40, "Other", template);
        Card c = new Card("diff", "Alice", 30, "Birthday", template);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, "not a card");
    }

    @Test
    public void toStringSummarizesCard() {
        Card card = new Card("Alice", 30, "Birthday", template);
        card.addSignature(new Signature("Bob", "hi"));
        String text = card.toString();
        assertTrue(text.contains("Alice"));
        assertTrue(text.contains("30"));
        assertTrue(text.contains("1 signature(s)"));
    }
}
