package de.poker.engine;

import de.poker.engine.utility.ComparisonConstants;

import java.util.Objects;

public class HoleCards {
    private Card card1;
    private Card card2;

    public static HoleCards of(String card1, String card2) {
        HoleCards holeCards = new HoleCards();

        Card tempC1 = Card.of(card1);
        Card tempC2 = Card.of(card2);
        if (tempC1.compareTo(tempC2) == ComparisonConstants.X_GREATER_THAN_Y) {
            holeCards.card1 = tempC1;
            holeCards.card2 = tempC2;
        } else {
            holeCards.card1 = tempC2;
            holeCards.card2 = tempC1;
        }
        return holeCards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoleCards holeCards = (HoleCards) o;
        return Objects.equals(card1, holeCards.card1) && Objects.equals(card2, holeCards.card2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(card1, card2);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(card1)
                .append(card2)
                .toString();
    }
}
