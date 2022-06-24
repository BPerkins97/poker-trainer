package de.poker.engine;

import de.poker.engine.utility.ComparisonConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

// TODO Cards of same value are equal
// TODO Cards of same value have same hash value
public class CardTest {
    @ParameterizedTest
    @ValueSource(strings = {"99", "65", "dd", "", "9d8d", "xx"})
    public void GivenAnIllegalString_WhenCreatingACard_ThenThrowAnException(String value) {
        try {
            Card.of(value);
            fail();
        } catch (AssertionError e) {
            assertTrue(true);
        }
    }

    @Test
    public void GivenNull_WhenCreatingACard_ThenThrowAnException() {
        try {
            Card.of(null);
            fail();
        } catch (AssertionError e) {
            assertTrue(true);
        }
    }

    @Test
    public void givenTwoSameCard_WhenComparingThem_TheyAreEqual() {
        Card card1 = Card.of("9h");
        Card card2 = Card.of("9h");

        assertEquals(card1, card2);
        assertEquals(card1.hashCode(), card2.hashCode());
    }

    @Test
    public void givenAhAndQd_WhenComparingThem_AhIsHigher() {
        Card card1 = Card.of("Ah");
        Card card2 = Card.of("Qd");

        assertEquals(ComparisonConstants.X_GREATER_THAN_Y, card1.compareTo(card2));
    }

    @Test
    public void testToString() {
        assertEquals("Ah", Card.of("Ah").toString());
    }
}
