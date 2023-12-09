package de.poker.solver.game;

import de.poker.trainer.solver.montecarlocfr.game.Card;
import de.poker.trainer.solver.montecarlocfr.game.Suit;
import de.poker.trainer.solver.montecarlocfr.game.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardTest {

    @Test
    public void test52Cards() {
        assertEquals(52, Card.NUM_CARDS);
        try {
            Card.of(52);
            Assertions.fail();
        } catch (Exception e) {
            // success
        }
    }

    @Test
    public void testAllCardsAreDistinct() {
        Set<Card> cards = new HashSet<>();
        for (int i=0;i<52;i++) {
            cards.add(Card.of(i));
        }

        Assertions.assertEquals(52, cards.size());
    }

    @Test
    public void testOrdering() {
        assertTrue(Card.of(Value.ACE, Suit.CLUB).compareTo(Card.of(Value.KING, Suit.CLUB)) > 0);
        assertTrue(Card.of(Value.QUEEN, Suit.CLUB).compareTo(Card.of(Value.TWO, Suit.CLUB)) > 0);
        assertTrue(Card.of(Value.ACE, Suit.CLUB).compareTo(Card.of(Value.QUEEN, Suit.CLUB)) > 0);
        assertTrue(Card.of(Value.ACE, Suit.CLUB).compareTo(Card.of(Value.ACE, Suit.DIAMOND)) > 0);
    }

    @Test
    public void testConstructorMethodsAreEqual() {
        Assertions.assertEquals(Card.of("Ac"), Card.of(51));
        Assertions.assertEquals(Card.of("Ac"), Card.of(Value.ACE, Suit.CLUB));

        Assertions.assertEquals(Card.of("2s"), Card.of(0));
        Assertions.assertEquals(Card.of("2s"), Card.of(Value.TWO, Suit.SPADES));
    }

    @Test
    public void testEquals()  {
        Assertions.assertEquals(Card.of("Ac"), Card.of("Ac"));
        Assertions.assertEquals(Card.of("Ac").hashCode(), Card.of("Ac").hashCode());
    }

    @Test
    public void testPresentation() {
        Assertions.assertEquals("Ad", Card.of(Value.ACE, Suit.DIAMOND).toString());
        Assertions.assertEquals("Qs", Card.of(Value.QUEEN, Suit.SPADES).toString());
        Assertions.assertEquals("Ah", Card.of(Value.ACE, Suit.HEART).toString());
    }

    @Test
    public void testRandomCard() {
        Random random = new Random(123L);
        Assertions.assertEquals(Card.of("9d"), Card.randomCard(random));
        Assertions.assertEquals(Card.of("5d"), Card.randomCard(random));
        Assertions.assertEquals(Card.of("5s"), Card.randomCard(random));
    }
}
