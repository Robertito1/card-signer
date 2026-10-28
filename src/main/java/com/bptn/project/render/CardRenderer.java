package com.bptn.project.render;

import com.bptn.project.model.Card;

/**
 * Turns a card into displayable text.
 */
public interface CardRenderer {

    String render(Card card);
}
