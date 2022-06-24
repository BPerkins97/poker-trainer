package de.poker.engine;

import java.util.Arrays;
import java.util.List;

public class Flop {
    private final Card card1;
    private final Card card2;
    private final Card card3;

    private Flop(Card card1, Card card2, Card card3) {
        this.card1 = card1;
        this.card2 = card2;
        this.card3 = card3;
    }

    public List<Card> cards() {
        return Arrays.asList(card1, card2, card3);
    }

    public static Flop of(String card1, String card2, String card3) {
        return new Flop(Card.of(card1), Card.of(card2), Card.of(card3));
    }
}
