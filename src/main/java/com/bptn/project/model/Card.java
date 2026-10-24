package com.bptn.project.model;


import com.bptn.project.model.template.CardTemplate;
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
    private int age;
    private String occasion;
    private final CardTemplate template;
    private final List<Signature> signatures = new ArrayList<>();

    public Card(String recipientName, int age, String occasion, CardTemplate template) {
        this(UUID.randomUUID().toString(), recipientName, age, occasion, template);
    }

    public Card(String id, String recipientName, int age, String occasion, CardTemplate template) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.template = Objects.requireNonNull(template, "template must not be null");
        setRecipientName(recipientName);
        setAge(age);
        setOccasion(occasion);
    }

    public String getId() {
        return id;
    }

    public String getRecipientName() {
        return recipientName;
    }

    // Validate
    public final void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public int getAge() {
        return age;
    }

    //Validate  
    public final void setAge(int age) {
        this.age = age;
    }

    public String getOccasion() {
        return occasion;
    }

    //Validate    
    public final void setOccasion(String occasion) {
        this.occasion = occasion;
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

    // Validate   
    private void checkSignatureIndex(int index) {
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
        return recipientName + " (turning " + age + ", " + occasion + ", "
            + signatures.size() + " signature(s))";
    }
}

