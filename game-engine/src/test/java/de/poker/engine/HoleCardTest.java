package de.poker.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HoleCardTest {
    @Test
    public void givenTwoSameHoleCards_WhenComparingThem_TheyAreTheSame() {
        HoleCards holeCards1 = HoleCards.of("9d", "6h");
        HoleCards holeCards2 = HoleCards.of("9d", "6h");

        assertEquals(holeCards1, holeCards2);
        assertEquals(holeCards1.hashCode(), holeCards2.hashCode());
    }

    @Test
    public void givenTwoSameCardsInDifferentOrder_WhenComparingThem_TheyAreTheSame() {
        HoleCards holeCards1 = HoleCards.of("9d", "6h");
        HoleCards holeCards2 = HoleCards.of("6h", "9d");

        assertEquals(holeCards1, holeCards2);
        assertEquals(holeCards1.hashCode(), holeCards2.hashCode());
    }
}
