package de.poker.solver.game;

import de.poker.solver.TestUtility;
import de.poker.trainer.solver.montecarlocfr.game.Action;
import de.poker.trainer.solver.montecarlocfr.game.Card;
import de.poker.trainer.solver.montecarlocfr.game.Constants;
import de.poker.trainer.solver.montecarlocfr.game.HoldEmGameTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HoldEmGameTreeTest {

    @Test
    public void testGameStartup() {
        Card[] cards = TestUtility.cardsToArray("Ah", "Ad", "Ks", "Qs", "6h", "5d", "3d", "2d", "Jh", "7c", "9c", "Tc", "Ac", "Kc", "Jd", "2h", "3h");

        HoldEmGameTree gameState = new HoldEmGameTree(cards);

        Assertions.assertTrue(gameState.isWinnerAtShowdown(0));
        Assertions.assertFalse(gameState.isWinnerAtShowdown(1));
        Assertions.assertFalse(gameState.isWinnerAtShowdown(2));
        Assertions.assertFalse(gameState.isWinnerAtShowdown(3));
        Assertions.assertFalse(gameState.isWinnerAtShowdown(4));
        Assertions.assertFalse(gameState.isWinnerAtShowdown(5));

        Assertions.assertEquals(Constants.SMALL_BLIND, gameState.getInvestment(0));
        Assertions.assertEquals(Constants.BIG_BLIND, gameState.getInvestment(1));
        Assertions.assertEquals(0, gameState.getInvestment(2));
        Assertions.assertEquals(0, gameState.getInvestment(3));
        Assertions.assertEquals(0, gameState.getInvestment(4));
        Assertions.assertEquals(0, gameState.getInvestment(5));

        Assertions.assertTrue(gameState.isCurrentPlayer(2));
        Assertions.assertEquals(Constants.SMALL_BLIND + Constants.BIG_BLIND, gameState.getPot());
        Assertions.assertFalse(gameState.isGameOver());
        Assertions.assertEquals("", gameState.history());
    }

    @Test
    public void everyoneFoldsToBigBlind() {
        Card[] cards = TestUtility.cardsToArray("Ah", "Ad", "Ks", "Qs", "6h", "5d", "3d", "2d", "Jh", "7c", "9c", "Tc", "Ac", "Kc", "Jd", "2h", "3h");

        HoldEmGameTree gameState = new HoldEmGameTree(cards);

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertEquals("f", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(3));

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertEquals("ff", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(4));

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertEquals("fff", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(5));

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertEquals("ffff", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(0));

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertEquals("fffff", gameState.history());
        Assertions.assertTrue(gameState.isGameOver());
    }

    @Test
    public void checkToShowdown() {
        Card[] cards = TestUtility.cardsToArray("Ah", "Ad", "Ks", "Qs", "6h", "5d", "3d", "2d", "Jh", "7c", "9c", "Tc", "Ac", "Kc", "Jd", "2h", "3h");

        HoldEmGameTree gameState = new HoldEmGameTree(cards);

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("c", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(3));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(4));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(5));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(0));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(1));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccccc", gameState.history());
        Assertions.assertEquals(1, gameState.bettingRound());
        Assertions.assertTrue(gameState.isCurrentPlayer(0));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(1));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(2));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(3));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(4));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(5));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(0));
        Assertions.assertEquals(2, gameState.bettingRound());

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(1));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(2));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(3));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(4));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccccccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(5));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccccccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(0));
        Assertions.assertEquals(3, gameState.bettingRound());

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccccccccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(1));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccccccccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(2));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccccccccccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(3));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccccccccccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(4));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("ccccccccccccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isCurrentPlayer(5));

        gameState = gameState.takeAction(Action.call());
        Assertions.assertEquals("cccccccccccccccccccccccc", gameState.history());
        Assertions.assertTrue(gameState.isGameOver());

        Assertions.assertEquals(10, gameState.getPayoffForPlayer(0));
        Assertions.assertEquals(-2, gameState.getPayoffForPlayer(1));
        Assertions.assertEquals(-2, gameState.getPayoffForPlayer(2));
        Assertions.assertEquals(-2, gameState.getPayoffForPlayer(3));
        Assertions.assertEquals(-2, gameState.getPayoffForPlayer(4));
        Assertions.assertEquals(-2, gameState.getPayoffForPlayer(5));
    }

    @Test
    public void checkPayoffWhenSomeoneFolds() {
        Card[] cards = TestUtility.cardsToArray("Ah", "Ad", "Ks", "Qs", "6h", "5d", "3d", "2d", "Jh", "7c", "9c", "Tc", "Ac", "Kc", "Jd", "2h", "3h");

        HoldEmGameTree gameState = new HoldEmGameTree(cards);

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertTrue(gameState.isGameOverForPlayer(2));
        Assertions.assertEquals(0, gameState.getPayoffForPlayer(2));
    }

    @Test
    public void checkPlayerLosesHisInvestmentWhenHeFolds() {
        Card[] cards = TestUtility.cardsToArray("Ah", "Ad", "Ks", "Qs", "6h", "5d", "3d", "2d", "Jh", "7c", "9c", "Tc", "Ac", "Kc", "Jd", "2h", "3h");

        HoldEmGameTree gameState = new HoldEmGameTree(cards);

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertTrue(gameState.isGameOverForPlayer(2));
        Assertions.assertEquals(0, gameState.getPayoffForPlayer(2));

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertTrue(gameState.isGameOverForPlayer(3));
        Assertions.assertEquals(0, gameState.getPayoffForPlayer(3));

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertTrue(gameState.isGameOverForPlayer(4));
        Assertions.assertEquals(0, gameState.getPayoffForPlayer(4));

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertTrue(gameState.isGameOverForPlayer(5));
        Assertions.assertEquals(0, gameState.getPayoffForPlayer(5));

        gameState = gameState.takeAction(Action.raise(50));
        Assertions.assertFalse(gameState.isGameOverForPlayer(0));
        Assertions.assertEquals("ffffr50", gameState.history());

        gameState = gameState.takeAction(Action.raise(100));
        Assertions.assertFalse(gameState.isGameOverForPlayer(1));
        Assertions.assertEquals(0, gameState.bettingRound());

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertTrue(gameState.isGameOverForPlayer(0));
        Assertions.assertTrue(gameState.isGameOverForPlayer(1));
        Assertions.assertEquals(-51, gameState.getPayoffForPlayer(0));
        Assertions.assertEquals(51, gameState.getPayoffForPlayer(1));
    }

    @Test
    public void testAllInImmediatelyToShowdown() {
        Card[] cards = TestUtility.cardsToArray("Ah", "Ad", "Ks", "Qs", "6h", "5d", "3d", "2d", "Jh", "7c", "9c", "Tc", "Ac", "Kc", "Jd", "2h", "3h");

        HoldEmGameTree gameState = new HoldEmGameTree(cards);

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertTrue(gameState.isGameOverForPlayer(2));
        Assertions.assertEquals(0, gameState.getPayoffForPlayer(2));

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertTrue(gameState.isGameOverForPlayer(3));
        Assertions.assertEquals(0, gameState.getPayoffForPlayer(3));

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertTrue(gameState.isGameOverForPlayer(4));
        Assertions.assertEquals(0, gameState.getPayoffForPlayer(4));

        gameState = gameState.takeAction(Action.fold());
        Assertions.assertTrue(gameState.isGameOverForPlayer(5));
        Assertions.assertEquals(0, gameState.getPayoffForPlayer(5));

        gameState = gameState.takeAction(Action.raise(199));
        Assertions.assertFalse(gameState.isGameOverForPlayer(0));
        Assertions.assertEquals("ffffr199", gameState.history());

        gameState = gameState.takeAction(Action.call());
        Assertions.assertTrue(gameState.isGameOverForPlayer(1));
        Assertions.assertTrue(gameState.isGameOverForPlayer(0));
        Assertions.assertEquals("ffffr199c", gameState.history());
        Assertions.assertEquals(200, gameState.getPayoffForPlayer(0));
        Assertions.assertEquals(-200, gameState.getPayoffForPlayer(1));
    }
}
