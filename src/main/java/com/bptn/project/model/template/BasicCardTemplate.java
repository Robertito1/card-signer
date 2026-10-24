package com.bptn.project.model.template;

/**
 * Minimal equals-sign bordered card for formal occasions.
 */
public class BasicCardTemplate extends CardTemplate {

    public BasicCardTemplate() {
        super("Elegant", '=', 46, "WITH BEST WISHES");
    }

    @Override
    public String decorationLine() {
        return "- . - . - . - . -";
    }
}