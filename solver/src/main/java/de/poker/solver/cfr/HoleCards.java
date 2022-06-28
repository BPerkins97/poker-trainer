package de.poker.solver.cfr;

import de.poker.solver.utility.ComparisonConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record HoleCards(Card card1, Card card2) {
    public static HoleCards of(String card1, String card2) {
        Card tempC1 = Card.of(card1);
        Card tempC2 = Card.of(card2);
        if (tempC1.compareTo(tempC2) == ComparisonConstants.X_GREATER_THAN_Y) {
            return new HoleCards(tempC1, tempC2);
        } else {
            return new HoleCards(tempC2, tempC1);
        }
    }

    public List<Card> cards() {
        return Arrays.asList(card1, card2);
    }

    public String asInfoSet() {
        return card1.forInfoSet() + card2.forInfoSet();
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
