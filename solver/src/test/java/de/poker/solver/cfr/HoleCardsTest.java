package de.poker.solver.cfr;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class HoleCardsTest {

    @ParameterizedTest
    @CsvSource({
            "Ah,As,A",
            "Qs,Js,QJs",
            "Qs,Jh,QJo",
            "Js,Qs,QJs",
            "Jh,Qs,QJo"
    })
    public void testHoleCardsToReducedInfoSet(String card1, String card2, String result) {
        HoleCards of = HoleCards.of(card1, card2);
        StringBuilder sb = new StringBuilder();
        of.appendReducedInfoSet(sb);
        Assertions.assertEquals(result, sb.toString());
    }
}
