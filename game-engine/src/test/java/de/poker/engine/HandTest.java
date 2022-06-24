package de.poker.engine;

import org.junit.jupiter.api.Test;

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

    @Test
    public void fullHouseTest() {
        Hand hand = Hand.of("As", "Ks", "Kd", "Ad", "Td", "Kh", "6s");

        assertEquals("Full House: Ks-Kh-Kd-As-Ad", hand.toString());
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
}
