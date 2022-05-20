package de.poker.trainer.engine;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PokerTest {

    @Test
    public void test() {
        List<Player> playerList = new ArrayList<>();
        playerList.add(new Player(1, Position.BIG_BLIND, 10));
        playerList.add(new Player(2, Position.SMALL_BLIND, 20));

        List<Integer> order = new ArrayList<>(52);
        for (int i=0;i<52;i++) {
            order.add(i);
        }

        GameState gameState = GameState.newGame(playerList, Deck.fullDeck(order));
        gameState = GameState.flop(gameState);
        gameState = GameState.turn(gameState);
        gameState = GameState.river(gameState);

        System.out.println(gameState);
    }
}
