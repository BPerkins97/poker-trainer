package de.poker.solver;

import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.game.Card;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HoldEmGameTreeTest {

    @Test
    public void afterCheckCheckItGoesToNextBettingRound() {
        Card[] cards = new Card[]{
                Card.of("As"), Card.of("Ks"), Card.of("4d"), Card.of("5d"), Card.of("8c"), Card.of("9d"), Card.of("Ts"), Card.of("8d"), Card.of("Tc")
        };
        HoldEmGameTree gameTree = new HoldEmGameTree(cards);
        HoldEmGameTree gameTree1 = gameTree.takeAction(1);
        HoldEmGameTree gameTree2 = gameTree1.takeAction(0);
        Assertions.assertEquals("cc", gameTree2.history);
        Assertions.assertEquals(1, gameTree2.bettingRound);
    }
}
