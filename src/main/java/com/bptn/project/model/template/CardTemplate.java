package com.bptn.project.model.template;

/**
 * Abstract visual style for a card. Subclasses provide the border character,
 * headline banner and a decorative ASCII line so new styles can be added
 * without touching rendering or menu code.
 */
public abstract class CardTemplate {

    public static final int MIN_WIDTH = 30;

    private final String name;
    private final char borderChar;
    private final int width;
    private final String banner;

    protected CardTemplate(String name, char borderChar, int width, String banner) {
        if (width < MIN_WIDTH) {
            throw new IllegalArgumentException("Card width must be at least " + MIN_WIDTH);
        }
        this.name = name;
        this.borderChar = borderChar;
        this.width = width;
        this.banner = banner;
    }

    /** Display name shown in the template picker. */
    public String getName() {
        return name;
    }

    /** Character used to draw the card border. */
    public char getBorderChar() {
        return borderChar;
    }

    /** Total card width in characters, including the border. */
    public int getWidth() {
        return width;
    }

    /** Headline printed near the top of the card. */
    public String getBanner() {
        return banner;
    }

    /** Decorative ASCII line drawn under the banner. */
    public abstract String decorationLine();
}

