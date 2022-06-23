package de.poker.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

// TODO Hole Cards are always sorted by value
public class GameTest {
    @Test
    @Disabled
    public void givenEmptyState_WhenCreatingANewGame_ThenEachPlayerHasHoleCards() {
        Game game = Game.withDeck(new CardDeck());

        Assertions.assertEquals(HoleCards.of("9d", "5h"), game.smallBlind().holeCards());
    }
}
