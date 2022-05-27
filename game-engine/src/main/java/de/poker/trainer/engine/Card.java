package de.poker.trainer.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record Card(CardValue value, CardSuit suit) {
    private static final List<Card> COMPLETE_DECK = new ArrayList<>(52);

    static {
        for (CardValue value : CardValue.values()) {
            for (CardSuit suit : CardSuit.values()) {
                COMPLETE_DECK.add(new Card(value, suit));
            }
        }
    }
    public static List<Card> randomlyShuffledDeck(int numCards) {
        List<Card> cards = new ArrayList<>(numCards);

        for (int i=0;i<numCards;i++) {
            int randomCard;
            do {
                randomCard = ThreadLocalRandom.current().nextInt(52);
            } while(cards.contains(COMPLETE_DECK.get(randomCard)));
            cards.add(COMPLETE_DECK.get(randomCard));
        }
        return cards;
    }
}
