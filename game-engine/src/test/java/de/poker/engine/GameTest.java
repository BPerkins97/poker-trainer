package de.poker.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameTest {
    @Test
    public void givenAValidGameSetup_WhenConstructingGame_ThenGetGame() {
        Game game = Game.Factory.newGame()
                .startingStacks(100)
                .smallBlind("9d", "5h")
                .bigBlind("9h", "Qd")
                .loJack("6h", "Kd")
                .hiJack("Ah", "As")
                .cutOff("Kc", "Qc")
                .button("Ac", "2s")
                .build();

        HoleCards of = HoleCards.of("9d", "5h");
        assertEquals(of, game.smallBlind().holeCards());
        assertEquals(HoleCards.of("Qd", "9h"), game.bigBlind().holeCards());
        assertEquals(HoleCards.of("Kd", "6h"), game.loJack().holeCards());
        assertEquals(HoleCards.of("Ah", "As"), game.hiJack().holeCards());
        assertEquals(HoleCards.of("Kc", "Qc"), game.cutOff().holeCards());
        assertEquals(HoleCards.of("Ac", "2s"), game.button().holeCards());
    }
}
