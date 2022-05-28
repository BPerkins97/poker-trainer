package de.poker.trainer.engine;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.poker.trainer.engine.Action.*;
import static de.poker.trainer.engine.CardSuit.*;
import static de.poker.trainer.engine.CardValue.*;
import static de.poker.trainer.engine.Position.*;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void PlayerBetsOutOfOrder() {
        List<Player> players = PlayerBuilder.builder()
                .stackSize(100)
                .build();

        List<Card> cards = DeckBuilder.random().build();

        try {
            GameState result = GameState.newGame(players, cards)
                    .takeAction(bet(BUTTON, 10));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("It's not your turn", e.getMessage());
        }
    }

    @Test
    @Disabled("Ignore this for now, we got more important stuff to do")
    public void playerChecksAfterSomeoneBet() {
        List<Player> players = PlayerBuilder.builder()
                .stackSize(100)
                .build();

        List<Card> cards = DeckBuilder.random().build();

        try {
            GameState result = GameState.newGame(players, cards)
                    .takeAction(bet(LOJACK, 10))
                    .takeAction(check(HIJACK));
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals("You can't check after somebody bet", e.getMessage());
        }
    }

    @Test
    public void pairVsPair() {
        List<Player> players = PlayerBuilder.builder()
                .stackSize(100)
                .build();

        List<Card> cards = DeckBuilder.random()
                .smallBlind(ACE, CLUB, ACE, HEART)
                .bigBlind(KING, CLUB, KING, DIAMOND)
                .flop(EIGHT, CLUB, SEVEN, DIAMOND, FIVE, CLUB)
                .turn(FOUR, HEART)
                .river(JACK, CLUB)
                .build();

        GameState result = checkdown(players, cards);

        assertEquals(102.0, result.players().stream().filter(player -> player.position().equals(SMALL_BLIND)).findFirst().get().stack());
        assertEquals(98.0, result.players().stream().filter(player -> player.position().equals(BIG_BLIND)).findFirst().get().stack());
    }

    @Test
    public void flushVsPair() {
        List<Player> players = PlayerBuilder.builder()
                .stackSize(100)
                .build();

        List<Card> cards = DeckBuilder.random()
                .smallBlind(ACE, CLUB, QUEEN, CLUB)
                .bigBlind(KING, CLUB, KING, DIAMOND)
                .flop(EIGHT, CLUB, SEVEN, DIAMOND, FIVE, CLUB)
                .turn(FOUR, HEART)
                .river(JACK, CLUB)
                .build();

        GameState result = checkdown(players, cards);

        assertEquals(102.0, result.players().stream().filter(player -> player.position().equals(SMALL_BLIND)).findFirst().get().stack());
        assertEquals(98.0, result.players().stream().filter(player -> player.position().equals(BIG_BLIND)).findFirst().get().stack());
    }

    private GameState checkdown(List<Player> players, List<Card> cards) {
        return GameState.newGame(players, cards)
                .takeAction(fold(LOJACK))
                .takeAction(fold(HIJACK))
                .takeAction(fold(CUTOFF))
                .takeAction(fold(BUTTON))
                .takeAction(call(SMALL_BLIND))
                .flop()
                .takeAction(check(SMALL_BLIND))
                .takeAction(check(BIG_BLIND))
                .turn()
                .takeAction(check(SMALL_BLIND))
                .takeAction(check(BIG_BLIND))
                .river()
                .takeAction(check(SMALL_BLIND))
                .takeAction(check(BIG_BLIND));
    }
}
