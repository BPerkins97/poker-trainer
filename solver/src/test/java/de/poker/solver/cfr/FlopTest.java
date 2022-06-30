package de.poker.solver.cfr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FlopTest {

    @ParameterizedTest
    @CsvSource({
            "Ah,Qd,Ts",
            "Ah,Ts,Qd",
            "Qd,Ah,Ts",
            "Qd,Ts,Ah",
            "Ts,Ah,Qd",
            "Ts,Qd,Ah"
    })
    public void testSort(String card1, String card2, String card3) {
        Flop flop = Flop.of(Card.of(card1), Card.of(card2), Card.of(card3));

        Assertions.assertEquals(Card.of("Ah"), flop.card1());
        Assertions.assertEquals(Card.of("Qd"), flop.card2());
        Assertions.assertEquals(Card.of("Ts"), flop.card3());
    }
}
