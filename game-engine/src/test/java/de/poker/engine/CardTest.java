package de.poker.engine;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {
    @ParameterizedTest
    @ValueSource(strings = {"99", "65", "dd", "", "9d8d"})
    public void GivenAnIllegalString_WhenCreatingACard_ThenThrowAnException(String value) {
        try {
            Card.of(value);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void GivenNull_WhenCreatingACard_ThenThrowAnException() {
        try {
            Card.of(null);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
