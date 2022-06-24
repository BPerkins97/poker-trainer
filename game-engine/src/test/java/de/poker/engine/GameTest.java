package de.poker.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO all in test
// TODO caller must go all in test
// TODO everyone is all in test
// TODO test reraise
// TODO test error raise then someone checks
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

    @Test
    public void givenEveryoneChecks_WhenGameEnds_StrongestHandCollectsTheBlinds() {
        Game game = Game.Factory.newGame()
                .startingStacks(100)
                .smallBlind("9d", "5h")
                .bigBlind("9h", "Qd")
                .loJack("6h", "Kd")
                .hiJack("Ah", "As")
                .cutOff("Kc", "Qc")
                .button("Ac", "2s")
                .flop("Qc", "Tc", "3c")
                .turn("5c")
                .river("5s")
                .build();

        // Preflop
        game.call(Player.Position.LO_JACK);
        game.call(Player.Position.HI_JACK);
        game.call(Player.Position.CUT_OFF);
        game.call(Player.Position.BUTTON);
        game.call(Player.Position.SMALL_BLIND);
        game.check(Player.Position.BIG_BLIND);

        // Postflop
        game.check(Player.Position.SMALL_BLIND);
        game.check(Player.Position.BIG_BLIND);
        game.check(Player.Position.LO_JACK);
        game.check(Player.Position.HI_JACK);
        game.check(Player.Position.CUT_OFF);
        game.check(Player.Position.BUTTON);

        // Turn
        game.check(Player.Position.SMALL_BLIND);
        game.check(Player.Position.BIG_BLIND);
        game.check(Player.Position.LO_JACK);
        game.check(Player.Position.HI_JACK);
        game.check(Player.Position.CUT_OFF);
        game.check(Player.Position.BUTTON);

        // River
        game.check(Player.Position.SMALL_BLIND);
        game.check(Player.Position.BIG_BLIND);
        game.check(Player.Position.LO_JACK);
        game.check(Player.Position.HI_JACK);
        game.check(Player.Position.CUT_OFF);
        game.check(Player.Position.BUTTON);

        Assertions.assertEquals(110, game.button().stack());
        Assertions.assertEquals(98, game.cutOff().stack());
        Assertions.assertEquals(98, game.loJack().stack());
        Assertions.assertEquals(98, game.hiJack().stack());
        Assertions.assertEquals(98, game.smallBlind().stack());
        Assertions.assertEquals(98, game.bigBlind().stack());
    }

    @Test
    public void givenItIsFoldedToSmallBlind_WhenSmallBlindFolds_ThenBigBlindWins() {
        Game game = Game.Factory.newGame()
                .startingStacks(100)
                .smallBlind("9d", "5h")
                .bigBlind("9h", "Qd")
                .loJack("6h", "Kd")
                .hiJack("Ah", "As")
                .cutOff("Kc", "Qc")
                .button("Ac", "2s")
                .flop("Qc", "Tc", "3c")
                .turn("5c")
                .river("5s")
                .build();

        // Preflop
        game.fold(Player.Position.LO_JACK);
        game.fold(Player.Position.HI_JACK);
        game.fold(Player.Position.CUT_OFF);
        game.fold(Player.Position.BUTTON);
        game.fold(Player.Position.SMALL_BLIND);

        Assertions.assertEquals(100, game.button().stack());
        Assertions.assertEquals(100, game.cutOff().stack());
        Assertions.assertEquals(100, game.loJack().stack());
        Assertions.assertEquals(100, game.hiJack().stack());
        Assertions.assertEquals(99, game.smallBlind().stack());
        Assertions.assertEquals(101, game.bigBlind().stack());
    }

    @Test
    public void givenARaiseAndCall_WhenItIsCheckedToShowdown_ThenStrongerHandWinsItAll() {
        Game game = Game.Factory.newGame()
                .startingStacks(100)
                .smallBlind("9d", "5h")
                .bigBlind("9h", "Qd")
                .loJack("6h", "Kd")
                .hiJack("Ah", "As")
                .cutOff("Kc", "Qc")
                .button("Ac", "2s")
                .flop("Qc", "Tc", "3c")
                .turn("5c")
                .river("5s")
                .build();

        // Preflop
        game.fold(Player.Position.LO_JACK);
        game.call(Player.Position.HI_JACK);
        game.call(Player.Position.CUT_OFF);
        game.raise(Player.Position.BUTTON, 5);
        game.call(Player.Position.SMALL_BLIND);
        game.fold(Player.Position.BIG_BLIND);
        game.fold(Player.Position.HI_JACK);
        game.fold(Player.Position.CUT_OFF);

        // Postflop
        game.check(Player.Position.SMALL_BLIND);
        game.check(Player.Position.BUTTON);

        // Turn
        game.check(Player.Position.SMALL_BLIND);
        game.check(Player.Position.BUTTON);

        // River
        game.check(Player.Position.SMALL_BLIND);
        game.check(Player.Position.BUTTON);

        Assertions.assertEquals(111, game.button().stack());
        Assertions.assertEquals(98, game.cutOff().stack());
        Assertions.assertEquals(100, game.loJack().stack());
        Assertions.assertEquals(98, game.hiJack().stack());
        Assertions.assertEquals(95, game.smallBlind().stack());
        Assertions.assertEquals(98, game.bigBlind().stack());
    }

    @Test
    public void givenARaiseAndCall_WhenItIsCheckedToShowdown_ThenPotIsShared() {
        Game game = Game.Factory.newGame()
                .startingStacks(100)
                .smallBlind("Qd", "2h")
                .bigBlind("9h", "Qd")
                .loJack("6h", "Kd")
                .hiJack("Ah", "As")
                .cutOff("Kc", "2c")
                .button("Qs", "2s")
                .flop("Qc", "Tc", "3c")
                .turn("5c")
                .river("5s")
                .build();

        // Preflop
        game.fold(Player.Position.LO_JACK);
        game.fold(Player.Position.HI_JACK);
        game.fold(Player.Position.CUT_OFF);
        game.raise(Player.Position.BUTTON, 5);
        game.call(Player.Position.SMALL_BLIND);
        game.fold(Player.Position.BIG_BLIND);

        // Postflop
        game.check(Player.Position.SMALL_BLIND);
        game.check(Player.Position.BUTTON);

        // Turn
        game.check(Player.Position.SMALL_BLIND);
        game.check(Player.Position.BUTTON);

        // River
        game.check(Player.Position.SMALL_BLIND);
        game.check(Player.Position.BUTTON);

        Assertions.assertEquals(101, game.button().stack());
        Assertions.assertEquals(100, game.cutOff().stack());
        Assertions.assertEquals(100, game.loJack().stack());
        Assertions.assertEquals(100, game.hiJack().stack());
        Assertions.assertEquals(101, game.smallBlind().stack());
        Assertions.assertEquals(98, game.bigBlind().stack());
    }
}
