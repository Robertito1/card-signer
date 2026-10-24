package com.bptn.project.io;

/**
 * Source of user input. Decouples the menu from the console so tests can
 * feed scripted answers.
 */
public interface InputProvider {

    /**
     * Prints the prompt and returns the line the user entered, or null when
     * the input is exhausted / EOF.
     */
    String readLine(String prompt);
}
