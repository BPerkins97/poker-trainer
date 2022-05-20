package de.poker.trainer.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.poker.trainer.engine.Action.fold;
import static de.poker.trainer.engine.Position.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PokerTest {

    @Test
    public void ActionFoldsToBigBlindPreFlop() {
        List<Player> players = PlayerBuilder.builder()
                .stackSize(100)
                .build();

        List<Card> cards = DeckBuilder.random().build();

        GameState result = GameState.newGame(players, cards)
                .takeAction(fold(LOJACK))
                .takeAction(fold(HIJACK))
                .takeAction(fold(CUTOFF))
                .takeAction(fold(BUTTON))
                .takeAction(fold(SMALL_BLIND));

        assertEquals(101, result.players().stream().filter(player -> player.position().equals(Position.BIG_BLIND)).findAny().get().stack());
        assertEquals(99, result.players().stream().filter(player -> player.position().equals(Position.SMALL_BLIND)).findAny().get().stack());
    }
}
