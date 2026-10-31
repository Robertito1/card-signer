package com.bptn.project.model;

import com.bptn.project.exception.ValidationException;
import com.bptn.project.model.template.CardTemplate;
import com.bptn.project.util.InputValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A greeting card: who it is for, what template it uses, and the signatures
 * people have added to it.
 */

public class Card {

    private final String id;
    private String recipientName;
    private Integer age;
    private String occasion;
    private final CardTemplate template;
    private final List<Signature> signatures = new ArrayList<>();

    public Card(String recipientName, Integer age, String occasion, CardTemplate template) {
        this(UUID.randomUUID().toString(), recipientName, age, occasion, template);
    }

    public Card(String id, String recipientName, Integer age, String occasion, CardTemplate template) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.template = Objects.requireNonNull(template, "template must not be null");
        setRecipientName(recipientName);
        setAge(age);
        setOccasion(occasion);
    }
    
    public Card(String recipientName, String occasion, CardTemplate template) {
        this(UUID.randomUUID().toString(), recipientName, occasion, template);
    }

    public Card(String id, String recipientName, String occasion, CardTemplate template) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.template = Objects.requireNonNull(template, "template must not be null");
        setRecipientName(recipientName);
        setOccasion(occasion);
    }
    public String getId() {
        return id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public final void setRecipientName(String recipientName) {
        this.recipientName = InputValidator.requireName(recipientName);
    }

    public Integer getAge() {
        return age;
    }

    public final void setAge(Integer age) {
    	InputValidator.checkAge(age);
        this.age = age;
    }

    public String getOccasion() {
        return occasion;
    }

    public final void setOccasion(String occasion) {
        this.occasion = InputValidator.requireText(
                occasion, "Occasion", InputValidator.MAX_OCCASION_LENGTH);;
    }

    public CardTemplate getTemplate() {
        return template;
    }

    public List<Signature> getSignatures() {
        return Collections.unmodifiableList(signatures);
    }

    public void addSignature(Signature signature) {
        signatures.add(Objects.requireNonNull(signature, "signature must not be null"));
    }

    public Signature getSignature(int index) {
        checkSignatureIndex(index);
        return signatures.get(index);
    }

    public void updateSignature(int index, String newWish) {
        getSignature(index).setWish(newWish);
    }

    public Signature removeSignature(int index) {
        checkSignatureIndex(index);
        return signatures.remove(index);
    }

    private void checkSignatureIndex(int index) {
    	 if (index < 0 || index >= signatures.size()) {
             throw new ValidationException(
                 "Signature number must be between 1 and " + signatures.size() + ".");
         }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Card other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
    	
        return this.occasion.toLowerCase().contains("birthday") ?
    		recipientName + "(turning " + age + ", " + occasion + ", "
            + signatures.size() + " signature(s))" :
            recipientName + " (on their " + ", " + occasion + ", "
            + signatures.size() + " signature(s))";
    }
}