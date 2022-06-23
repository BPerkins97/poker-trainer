package de.poker.engine;

import java.util.ArrayList;
import java.util.List;

public class CardDeck {
    private final List<Card> cards = new ArrayList<>();
    private int stackIndex = 0;

    public void addCard(Card card) {
        cards.add(card);
    }

    public Card nextCard() {
        Card nextCard = cards.get(stackIndex);
        stackIndex++;
        return nextCard;
    }
}
