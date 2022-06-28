package de.poker.solver.cfr;

import java.util.Arrays;
import java.util.List;

// TODO order the cards by value and suit
public record Flop(Card card1, Card card2, Card card3) {
    public List<Card> cards() {
        return Arrays.asList(card1, card2, card3);
    }

    public static Flop of(String card1, String card2, String card3) {
        return new Flop(Card.of(card1), Card.of(card2), Card.of(card3));
    }
}
