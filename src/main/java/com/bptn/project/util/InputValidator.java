package com.bptn.project.util;

import com.bptn.project.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Central place for all input validation rules.
 */
public final class InputValidator {

    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_OCCASION_LENGTH = 30;
    public static final int MAX_WISH_LENGTH = 200;
    public static final int MAX_AGE = 150;

    private static final Pattern NAME_PATTERN = Pattern.compile("[\\p{L} .'-]+");

    private InputValidator() {
    }

    /**
     * Validates a person's name: non-blank, bounded length, letters and common
     * name punctuation only. Returns the trimmed value.
     */
    public static String requireName(String raw) {
        String value = requireText(raw, "Name", MAX_NAME_LENGTH);
        if (!NAME_PATTERN.matcher(value).matches()) {
            throw new ValidationException(
                "Name may only contain letters, spaces, apostrophes, hyphens and periods.");
        }
        return value;
    }

    /**
     * Validates a general single-line text field: non-null, non-blank after
     * trimming, at most {@code max} characters. Control characters are
     * replaced with spaces so the value is always safe to store on one line.
     */
    public static String requireText(String raw, String field, int max) {
        if (raw == null) {
            throw new ValidationException(field + " must not be empty.");
        }
        String value = raw.replaceAll("\\p{Cntrl}", " ").trim();
        if (value.isEmpty()) {
            throw new ValidationException(field + " must not be empty.");
        }
        if (value.length() > max) {
            throw new ValidationException(field + " must be at most " + max + " characters.");
        }
        return value;
    }

    /**
     * Parses and range-checks an age between 0 and {@value #MAX_AGE}.
     */
    public static int parseAge(String raw) {
        int age = parseInt(raw, "Age");
        checkAge(age);
        return age;
    }

    /**
     * Range-checks an already-parsed age.
     */
    public static void checkAge(int age) {
        if (age < 0 || age > MAX_AGE) {
            throw new ValidationException("Age must be between 0 and " + MAX_AGE + ".");
        }
    }

    /**
     * Parses a 1-based menu / list choice bounded by {@code max}.
     */
    public static int parseChoice(String raw, int max) {
        int choice = parseInt(raw, "Choice");
        if (choice < 1 || choice > max) {
            throw new ValidationException("Please enter a number between 1 and " + max + ".");
        }
        return choice;
    }

    /**
     * Parses an integer, reporting {@code field} in the error message.
     */
    public static int parseInt(String raw, String field) {
        String value = raw == null ? "" : raw.trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(field + " must be a whole number.");
        }
    }

    /**
     * Parses a yes/no answer. Accepts y, yes, n, no (case-insensitive).
     */
    public static boolean parseYesNo(String raw) {
        String value = raw == null ? "" : raw.trim().toLowerCase();
        switch (value) {
            case "y":
            case "yes":
                return true;
            case "n":
            case "no":
                return false;
            default:
                throw new ValidationException("Please answer 'y' or 'n'.");
        }
    }
}
