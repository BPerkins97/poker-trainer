package de.poker.solver.utility;

import de.poker.solver.game.Card;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardInfoSetTest {

    @Test
    public void testCases() {
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
}
