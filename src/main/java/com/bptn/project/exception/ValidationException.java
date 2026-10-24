package com.bptn.project.exception;

/**
 * Thrown when user input or a domain value fails validation.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}

