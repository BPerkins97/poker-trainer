package de.poker.engine;

import de.poker.engine.utility.ComparisonConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HandTest {
    @Test
    public void highCardTest() {
        Hand hand = Hand.of("As", "Ks", "Jd", "Td", "9h", "6c", "7c");

        assertEquals("High Card: As-Ks-Jd-Td-9h", hand.toString());
    }

    @Test
    public void pairTest() {
        Hand hand = Hand.of("As", "Ad", "Ks", "Jd", "Td", "9h", "6c");

        assertEquals("Pair: As-Ad-Ks-Jd-Td", hand.toString());
    }

    @Test
    public void twoPairTest() {
        Hand hand = Hand.of("As", "Ad", "Ks", "Kd", "Td", "9h", "6c");

        assertEquals("Two Pair: As-Ad-Ks-Kd-Td", hand.toString());
    }

    @Test
    public void threeOfAKindTest() {
        Hand hand = Hand.of("As", "Ad", "Ah", "Kd", "Td", "9h", "6c");

        assertEquals("Three of a kind: As-Ah-Ad-Kd-Td", hand.toString());
    }

    @Test
    public void straightTest() {
        Hand hand = Hand.of("As", "Kd", "Qh", "Jd", "Td", "9h", "6c");

        assertEquals("Straight: As-Kd-Qh-Jd-Td", hand.toString());
    }

    @Test
    public void flushTest() {
        Hand hand = Hand.of("As", "Ks", "Qs", "Jd", "Td", "9s", "6s");

        assertEquals("Flush: As-Ks-Qs-9s-6s", hand.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "As-Ks-Kd-Ad-Td-Kh-6s,Full House: Ks-Kh-Kd-As-Ad",
            "As-Ks-Kd-Ad-Ac-Kc-6s,Full House: As-Ad-Ac-Ks-Kd",
            "As-Ks-Kd-Ad-6c-Kc-6s,Full House: Ks-Kd-Kc-As-Ad"
    })
    public void fullHouseTest(String inputs, String expected) {
        Hand hand = buildHand(inputs);

        assertEquals(expected, hand.toString());
    }

    @Test
    public void quadsTest() {
        Hand hand = Hand.of("Kc", "Ks", "Kd", "Ad", "Td", "Kh", "6s");

        assertEquals("Quads: Ks-Kh-Kd-Kc-Ad", hand.toString());
    }

    @Test
    public void straightFlushTest() {
        Hand hand = Hand.of("Qs", "Ks", "Js", "Ad", "Td", "9s", "Ts");

        assertEquals("Straight Flush: Ks-Qs-Js-Ts-9s", hand.toString());
    }

    @Test
    public void royalFlushTest() {
        Hand hand = Hand.of("Qs", "Ks", "Js", "As", "Td", "9s", "Ts");

        assertEquals("Royal Flush: As-Ks-Qs-Js-Ts", hand.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "As-Ks-Qs-Js-Ts-9s-8s,Ah-Kh-Qs-Jh-9c-8c-7s",
            "Ks-Qs-Jd-Td-9h-6h-5c,8h-7c-6c-5c-4d-Ah-Qd"
    })
    public void compareUnequalHands(String strongerHand, String weakerHand) {
        Hand hand = buildHand(strongerHand);
        Hand hand1 = buildHand(weakerHand);

        Assertions.assertEquals(ComparisonConstants.X_GREATER_THAN_Y, hand.compareTo(hand1));
    }

    @ParameterizedTest
    @CsvSource({
            "As-Ks-Qs-Js-Ts-9s-8s,Ah-Kh-Qh-Jh-Th-9h-8h"
    })
    public void compareEqualHands(String hand1, String hand2) {
        Hand hand3 = buildHand(hand1);
        Hand hand4 = buildHand(hand2);

        Assertions.assertEquals(ComparisonConstants.X_EQUAL_TO_Y, hand3.compareTo(hand4));
    }

    private Hand buildHand(String input) {
        return Hand.of(input.split("-"));
    }
}
