package de.poker.solver.utility;

import de.poker.solver.game.Card;
import de.poker.solver.game.Suit;
import de.poker.solver.game.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardInfoSetTest {

    @Test
    public void testHoleCards() {
        Set<Long> longs = new HashSet<>();

        for (Card card1 : Card.CARDS) {
            for (Card card2 : Card.CARDS) {
                if (card1.equals(card2)) {
                    continue;
                }
                long result = CardInfoSetBuilder.toInfoSet(Arrays.asList(card1, card2));
                longs.add(result);
            }
        }

        Assertions.assertEquals(169, longs.size());
    }

    @Test
    public void testFlop() {
        Set<Long> longs = new HashSet<>();

        Card holeCard1 = Card.of(Value.ACE, Suit.CLUB);
        Card holeCard2 = Card.of(Value.KING, Suit.CLUB);

        for (Card card1 : Card.CARDS) {
            if (card1.equals(holeCard1) || card1.equals(holeCard2)) {
                continue;
            }
            for (Card card2 : Card.CARDS) {
                if (card1.equals(card2) || card2.equals(holeCard1) || card2.equals(holeCard2)) {
                    continue;
                }
                for (Card card3 : Card.CARDS) {
                    if (card3.equals(card1) || card3.equals(card2) || card3.equals(holeCard1) || card3.equals(holeCard2)) {
                        continue;
                    }
                    long result = CardInfoSetBuilder.toInfoSet(Arrays.asList(holeCard1, holeCard2, card1, card2, card3));
                    longs.add(result);
                }
            }
        }

        Assertions.assertEquals(4650, longs.size());
    }
}
