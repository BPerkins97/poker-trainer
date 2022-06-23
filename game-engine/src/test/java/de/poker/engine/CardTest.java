package de.poker.engine;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {
    @Test
    public void GivenAnIllegalString_WhenCreatingACard_ThenThrowAnException() {
        try {
            Card.of("99");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("99 is not a valid card.", e.getMessage());
        }
    }
}
