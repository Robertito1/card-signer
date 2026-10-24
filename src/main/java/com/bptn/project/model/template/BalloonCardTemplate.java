package com.bptn.project.model.template;

/**
 * Hash-bordered party card with balloon decorations.
 */
public class BalloonCardTemplate extends CardTemplate {

    public BalloonCardTemplate() {
        super("Balloon Party", '#', 46, "LET'S CELEBRATE!");
    }

    @Override
    public String decorationLine() {
        return "( o )   ( o )   ( o )";
    }
}
