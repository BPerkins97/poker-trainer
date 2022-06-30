package de.poker.solver.cfr;

import de.poker.solver.utility.ComparisonConstants;

import java.util.Arrays;
import java.util.List;

// TODO think about how we could reduce this state further
record Flop(Card card1, Card card2, Card card3) {

    // Return a sorted flop
    public static Flop of(Card card1, Card card2, Card card3) {

        int card1ToCard2 = card1.compareTo(card2);
        int card2ToCard3 = card2.compareTo(card3);
        int card1ToCard3 = card1.compareTo(card3);
        if (card1ToCard2 == ComparisonConstants.X_GREATER_THAN_Y) {
            if (card2ToCard3 == ComparisonConstants.X_GREATER_THAN_Y) {
                return new Flop(card1, card2, card3);
            }
            if (card1ToCard3 == ComparisonConstants.X_GREATER_THAN_Y) {
                return new Flop(card1, card3, card2);
            }
            return new Flop(card3, card1, card2);
        }
        if (card2ToCard3 == ComparisonConstants.X_GREATER_THAN_Y) {
            if (card1ToCard3 == ComparisonConstants.X_GREATER_THAN_Y) {
                return new Flop(card2, card1, card3);
            } else {
                return new Flop(card2, card3, card1);
            }
        }
        return new Flop(card3, card2, card1);
    }

    public List<Card> cards() {
        return Arrays.asList(card1, card2, card3);
    }
}
