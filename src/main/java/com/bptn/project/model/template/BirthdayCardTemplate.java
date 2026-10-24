package com.bptn.project.model.template;

/**
 * Classic star-bordered birthday card.
 */
public class BirthdayCardTemplate extends CardTemplate {

    public BirthdayCardTemplate() {
        super("Classic Birthday", '*', 46, "HAPPY BIRTHDAY!");
    }

    @Override
    public String decorationLine() {
        return "\\o/   \\o/   \\o/";
    }
}
