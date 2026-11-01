package com.bptn.project.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bptn.project.exception.ValidationException;
import org.junit.jupiter.api.Test;

public class SignatureTest {

    @Test
    public void createsValidSignature() {
        Signature s = new Signature("Bob", "Have a great year!");
        assertEquals("Bob", s.getSignerName());
        assertEquals("Have a great year!", s.getWish());
    }

    @Test
    public void rejectsInvalidSignerName() {
        assertThrows(ValidationException.class, () -> new Signature("  ", "wish"));
        assertThrows(ValidationException.class, () -> new Signature("Bob123", "wish"));
    }

    @Test
    public void rejectsBlankWish() {
        assertThrows(ValidationException.class, () -> new Signature("Bob", " "));
        assertThrows(ValidationException.class, () -> new Signature("Bob", null));
    }

    @Test
    public void setWishUpdatesAndValidates() {
        Signature s = new Signature("Bob", "old");
        s.setWish("new wish");
        assertEquals("new wish", s.getWish());
        assertThrows(ValidationException.class, () -> s.setWish(""));
    }

    @Test
    public void equalityBasedOnContent() {
        assertEquals(new Signature("Bob", "hi"), new Signature("Bob", "hi"));
        assertEquals(new Signature("Bob", "hi").hashCode(), new Signature("Bob", "hi").hashCode());
        assertNotEquals(new Signature("Bob", "hi"), new Signature("Bob", "yo"));
        assertNotEquals(new Signature("Bob", "hi"), new Signature("Ann", "hi"));
        assertNotEquals(new Signature("Bob", "hi"), "not a signature");
    }

    @Test
    public void toStringShowsSignerAndWish() {
        assertEquals("Bob: hi", new Signature("Bob", "hi").toString());
    }
}
