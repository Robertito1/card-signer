package com.bptn.project.model.template;


import com.bptn.project.exception.ValidationException;
import java.util.List;

/**
 * Registry of the card templates offered to the user. Adding a new template
 * means adding one entry here and one subclass - nothing else changes.
 */
public final class TemplateCatalog {

    private static final List<CardTemplate> TEMPLATES = List.of(
        new BirthdayCardTemplate(),
        new BalloonCardTemplate(),
        new BasicCardTemplate()
    );

    private TemplateCatalog() {
    }

    public static List<CardTemplate> all() {
        return TEMPLATES;
    }

    /**
     * Returns the template at 1-based {@code index} as shown in the picker.
     */
    public static CardTemplate get(int index) {
        if (index < 1 || index > TEMPLATES.size()) {
            throw new ValidationException(
                "Please choose a template between 1 and " + TEMPLATES.size() + ".");
        }
        return TEMPLATES.get(index - 1);
    }

    /** Stable key written to disk for a template. */
    public static String keyOf(CardTemplate template) {
        return template.getClass().getSimpleName();
    }

    /** Resolves a stored key back to a template instance. */
    public static CardTemplate byKey(String key) {
        return TEMPLATES.stream()
            .filter(t -> keyOf(t).equals(key))
            .findFirst()
            .orElseThrow(() -> new ValidationException("Unknown template: " + key));
    }
}

