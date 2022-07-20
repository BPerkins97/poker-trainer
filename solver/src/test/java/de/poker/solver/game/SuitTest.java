package de.poker.solver.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class SuitTest {

    @Test
    public void testExactly4Suits() {
        Assertions.assertEquals(4, Suit.values().length);
        Assertions.assertEquals(4, Suit.suitsInOrder().length);
    }

    @Test
    public void testOrdering() {
        Assertions.assertTrue(Suit.CLUB.compareTo(Suit.DIAMOND) > 0);
        Assertions.assertTrue(Suit.DIAMOND.compareTo(Suit.HEART) > 0);
        Assertions.assertTrue(Suit.HEART.compareTo(Suit.SPADES) > 0);
    }

    @Test
    public void testValue() {
        Assertions.assertEquals(0, Suit.SPADES.value());
        Assertions.assertEquals(1, Suit.HEART.value());
        Assertions.assertEquals(2, Suit.DIAMOND.value());
        Assertions.assertEquals(3, Suit.CLUB.value());
    }

    @Test
    public void testPresentation() {
        Assertions.assertEquals("s", Suit.SPADES.toString());
        Assertions.assertEquals("h", Suit.HEART.toString());
        Assertions.assertEquals("d", Suit.DIAMOND.toString());
        Assertions.assertEquals("c", Suit.CLUB.toString());
    }

    @Test
    public void testSuitsInOrder() {
        Suit[] suits = Suit.suitsInOrder();
        Suit s = suits[0];
        for (int i=1;i<4;i++) {
            Assertions.assertTrue(suits[i].compareTo(s) > 0);
            s = suits[i];
        }
    }

    @Test
    public void testParse() {
        Assertions.assertEquals(Suit.SPADES, Suit.parse("s"));
        Assertions.assertEquals(Suit.DIAMOND, Suit.parse("d"));
        Assertions.assertEquals(Suit.HEART, Suit.parse("h"));
        Assertions.assertEquals(Suit.CLUB, Suit.parse("c"));
        try {
            Suit.parse("invalid");
            Assertions.fail();
        } catch (Exception e) {

        }
    }
}
