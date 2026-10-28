package com.bptn.project.repository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bptn.project.model.Card;

/**
 * Keeps cards in a map. Used by tests and handy for trying the app without
 * touching disk.
 */
public class InMemoryCardRepository implements CardRepository {

    private final Map<String, Card> cards = new LinkedHashMap<>();

    @Override
    public void save(Card card) {
        cards.put(card.getId(), card);
    }

    @Override
    public List<Card> findAll() {
        return cards.values().stream()
            .sorted(Comparator.comparing(c -> c.getRecipientName().toLowerCase()))
            .toList();
    }

    @Override
    public Optional<Card> findById(String id) {
        return Optional.ofNullable(cards.get(id));
    }

    @Override
    public boolean deleteById(String id) {
        return cards.remove(id) != null;
    }
}