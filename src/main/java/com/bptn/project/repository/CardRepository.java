package com.bptn.project.repository;

import java.util.List;
import java.util.Optional;

import com.bptn.project.model.Card;

/**
 * Persistence abstraction for cards. Implementations may store cards in
 * memory, on disk, or anywhere else without the rest of the app changing.
 */
public interface CardRepository {

    /** Saves a card, overwriting any existing card with the same id. */
    void save(Card card);

    /** Returns all stored cards ordered by recipient name. */
    List<Card> findAll();

    Optional<Card> findById(String id);

    /** Deletes the card with the given id. Returns true if one existed. */
    boolean deleteById(String id);
}
