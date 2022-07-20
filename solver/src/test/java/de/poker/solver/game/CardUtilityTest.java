package de.poker.solver.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class CardUtilityTest {

    @Test
    public void testNormalization() {
        List<Card> cards = new ArrayList<>();
        cards.add(Card.of("Qs"));
        cards.add(Card.of("Ac"));
        cards.add(Card.of("Qd"));
        cards.add(Card.of("Qh"));
        cards.add(Card.of("Kd"));
        cards.add(Card.of("Td"));
        cards.add(Card.of("Tc"));

        CardUtils.normalizeInPlace(cards);

        Assertions.assertEquals(Card.of("As"), cards.get(0));
        Assertions.assertEquals(Card.of("Qh"), cards.get(1));
        Assertions.assertEquals(Card.of("Kd"), cards.get(2));
        Assertions.assertEquals(Card.of("Qd"), cards.get(3));
        Assertions.assertEquals(Card.of("Qc"), cards.get(4));
        Assertions.assertEquals(Card.of("Td"), cards.get(5));
        Assertions.assertEquals(Card.of("Ts"), cards.get(6));
    }

    @Test
    public void testCards2Long() {
        List<Card> cards = new ArrayList<>();
        cards.add(Card.of("Ks"));
        cards.add(Card.of("As"));
        cards.add(Card.of("Td"));
        long v = CardUtils.cardsToLong(cards, 3);
        List<Card> result = CardUtils.longToCards(v);
        Assertions.assertEquals(cards, result);
    }
}
