package com.bptn.project.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bptn.project.exception.ValidationException;
import org.junit.jupiter.api.Test;

public class InputValidatorTest {

    @Test
    public void requireNameTrimsWhitespace() {
        assertEquals("Alice", InputValidator.requireName("  Alice  "));
    }

    @Test
    public void requireNameAllowsCommonPunctuation() {
        assertEquals("Mary-Jane O'Brien Jr.", InputValidator.requireName("Mary-Jane O'Brien Jr."));
    }

    @Test
    public void requireNameRejectsBlank() {
        assertThrows(ValidationException.class, () -> InputValidator.requireName("   "));
    }

    @Test
    public void requireNameRejectsNull() {
        assertThrows(ValidationException.class, () -> InputValidator.requireName(null));
    }

    @Test
    public void requireNameRejectsDigitsAndSymbols() {
        assertThrows(ValidationException.class, () -> InputValidator.requireName("Al1ce"));
        assertThrows(ValidationException.class, () -> InputValidator.requireName("Alice!"));
        assertThrows(ValidationException.class, () -> InputValidator.requireName("Alice|Bob"));
    }

    @Test
    public void requireNameRejectsTooLong() {
        assertThrows(ValidationException.class,
            () -> InputValidator.requireName("A".repeat(InputValidator.MAX_NAME_LENGTH + 1)));
    }

    @Test
    public void requireTextReturnsTrimmedValue() {
        assertEquals("hello", InputValidator.requireText(" hello ", "Field", 10));
    }

    @Test
    public void requireTextStripsControlCharacters() {
        assertEquals("a b", InputValidator.requireText("a\tb", "Field", 10));
        assertEquals("a b", InputValidator.requireText("a\nb", "Field", 10));
    }

    @Test
    public void requireTextRejectsNullAndBlank() {
        assertThrows(ValidationException.class, () -> InputValidator.requireText(null, "Field", 5));
        assertThrows(ValidationException.class, () -> InputValidator.requireText("  ", "Field", 5));
    }

    @Test
    public void requireTextRejectsOverMax() {
        assertThrows(ValidationException.class, () -> InputValidator.requireText("toolong", "Field", 3));
    }

    @Test
    public void parseAgeAcceptsBounds() {
        assertEquals(0, InputValidator.parseAge("0"));
        assertEquals(30, InputValidator.parseAge(" 30 "));
        assertEquals(InputValidator.MAX_AGE, InputValidator.parseAge("150"));
    }

    @Test
    public void parseAgeRejectsBadValues() {
        assertThrows(ValidationException.class, () -> InputValidator.parseAge("abc"));
        assertThrows(ValidationException.class, () -> InputValidator.parseAge(""));
        assertThrows(ValidationException.class, () -> InputValidator.parseAge(null));
        assertThrows(ValidationException.class, () -> InputValidator.parseAge("-1"));
        assertThrows(ValidationException.class, () -> InputValidator.parseAge("151"));
        assertThrows(ValidationException.class, () -> InputValidator.parseAge("3.5"));
    }

    @Test
    public void checkAgeValidatesRange() {
        InputValidator.checkAge(0);
        InputValidator.checkAge(75);
        InputValidator.checkAge(InputValidator.MAX_AGE);
        assertThrows(ValidationException.class, () -> InputValidator.checkAge(-1));
        assertThrows(ValidationException.class, () -> InputValidator.checkAge(151));
    }

    @Test
    public void parseChoiceAcceptsInRange() {
        assertEquals(1, InputValidator.parseChoice("1", 5));
        assertEquals(5, InputValidator.parseChoice(" 5", 5));
    }

    @Test
    public void parseChoiceRejectsOutOfRangeAndNonNumeric() {
        assertThrows(ValidationException.class, () -> InputValidator.parseChoice("0", 5));
        assertThrows(ValidationException.class, () -> InputValidator.parseChoice("6", 5));
        assertThrows(ValidationException.class, () -> InputValidator.parseChoice("x", 5));
        assertThrows(ValidationException.class, () -> InputValidator.parseChoice(null, 5));
    }

    @Test
    public void parseIntParsesAndReportsField() {
        assertEquals(42, InputValidator.parseInt("42", "Field"));
        ValidationException e = assertThrows(ValidationException.class,
            () -> InputValidator.parseInt("x", "MyField"));
        assertTrue(e.getMessage().contains("MyField"));
    }

    @Test
    public void parseYesNoAcceptsVariants() {
        assertTrue(InputValidator.parseYesNo("y"));
        assertTrue(InputValidator.parseYesNo(" YES "));
        assertFalse(InputValidator.parseYesNo("n"));
        assertFalse(InputValidator.parseYesNo("No"));
        assertThrows(ValidationException.class, () -> InputValidator.parseYesNo("maybe"));
        assertThrows(ValidationException.class, () -> InputValidator.parseYesNo(null));
    }
}
