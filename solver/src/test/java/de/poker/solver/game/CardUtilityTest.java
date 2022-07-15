package de.poker.solver.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardUtilityTest {

    @ParameterizedTest
    @CsvSource({
            "168,Ac,Ad",
            "0,2c,2d",
            "5,4c,2c"
    })
    public void testHoleCardsToInt(int expected, String c1, String c2) {
        Card card1 = Card.of(c1);
        Card card2 = Card.of(c2);

        int actual = CardUtils.holeCardsToInt(card1, card2);
        Assertions.assertEquals(expected, actual);
    }

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

        Assertions.assertEquals(Card.of("Ac"), cards.get(0));
        Assertions.assertEquals(Card.of("Qd"), cards.get(1));
        Assertions.assertEquals(Card.of("Kh"), cards.get(2));
        Assertions.assertEquals(Card.of("Qh"), cards.get(3));
        Assertions.assertEquals(Card.of("Qs"), cards.get(4));
        Assertions.assertEquals(Card.of("Th"), cards.get(5));
        Assertions.assertEquals(Card.of("Tc"), cards.get(6));
    }

    @Test
    public void testSomething() {
        Map<Card, Map<Card, List<Card>>> cardMap = new HashMap<>();
        List<List<Card>> cards = new ArrayList<>();
        for (int i=0;i<50;i++) {
            Card cardI = Card.of(i);
            cardMap.put(cardI, new HashMap<>());
            for (int j=i+1;j<51;j++) {
                Card cardJ = Card.of(j);
                cardMap.get(cardI).put(cardJ, new ArrayList<>());
                for (int k=j+1;k<52;k++) {
                    cardMap.get(cardI).get(cardJ).add(Card.of(k));
                }
            }
        }
        System.out.println(cards.size());
    }
}
