package de.poker.engine;

import java.util.ArrayList;
import java.util.List;

import static de.poker.engine.utility.Assert.assertThat;

public class CardDeck {
    private final List<Card> cards = new ArrayList<>();
    private int stackIndex = 0;

    public void addCard(Card card) {
        cards.add(card);
    }

    public Card drawCard() {
        assertThat(stackIndex < cards.size(), "There are no cards to draw");

        Card nextCard = cards.get(stackIndex);
        stackIndex++;
        return nextCard;
    }
}
