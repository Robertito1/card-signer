package com.bptn.project.model;

import com.bptn.project.util.InputValidator;
import java.util.Objects;

/**
 * One person's signature on a card: who signed it and what they wrote.
 */
public class Signature {

    private final String signerName;
    private String wish;

    public Signature(String signerName, String wish) {
        this.signerName = InputValidator.requireName(signerName);
        setWish(wish);
    }

    public String getSignerName() {
        return signerName;
    }

    public String getWish() {
        return wish;
    }

    public void setWish(String wish) {
        this.wish = InputValidator.requireText(wish, "Wish", InputValidator.MAX_WISH_LENGTH);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Signature other)) {
            return false;
        }
        return signerName.equals(other.signerName) && wish.equals(other.wish);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signerName, wish);
    }

    @Override
    public String toString() {
        return signerName + ": " + wish;
    }
}

