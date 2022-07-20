package de.poker.solver.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.poker.solver.TestUtility.evaluateCards;

public class HandEvaluatorTest {
    @Test
    public void testHandRankOrdering() {
        // Royal flush vs Straight flush
        Assertions.assertTrue(evaluateCards("Ah", "Kh", "Qh", "Jh", "Th", "9d", "8d") > evaluateCards("Kd", "Qd", "Jd", "Td", "9d", "8h", "7h"));
        // Straight flush vs Four of a kind
        Assertions.assertTrue(evaluateCards("9h", "Kh", "Qh", "Jh", "Th", "9d", "8d") > evaluateCards("9h", "Kh", "Qh", "Jh", "9c", "9d", "9s"));
        // Four of a kind vs Full house
        Assertions.assertTrue(evaluateCards("9h", "Kh", "Qh", "Jh", "9c", "9d", "9s") > evaluateCards("9h", "Kh", "Qh", "Jh", "Jc", "9d", "9s"));
        // Full house vs flush
        Assertions.assertTrue(evaluateCards("9h", "Kh", "Qh", "Jh", "Jc", "9d", "9s") > evaluateCards("Kd", "Qd", "Jd", "Td", "7d", "7s", "7h"));
        // Flush vs Straight
        Assertions.assertTrue(evaluateCards("9h", "Kh", "Qh", "Jh", "Jc", "9d", "9s") > evaluateCards("Kd", "Qd", "Jd", "Td", "9s", "7s", "7h"));
        // Straight vs Trips
        Assertions.assertTrue(evaluateCards("Kd", "Qd", "Jd", "Td", "9s", "7s", "7h") > evaluateCards("Kd", "Qs", "Jd", "Td", "7d", "7s", "7h"));
        // Trips vs two pair
        Assertions.assertTrue(evaluateCards("Kd", "Qd", "7d", "Td", "9s", "7s", "7h") > evaluateCards("Kd", "Ks", "Jd", "Td", "7d", "7s", "6h"));
        // Two pair vs one pair
        Assertions.assertTrue(evaluateCards("Kd", "Ks", "Jd", "Td", "7d", "7s", "6h") > evaluateCards("Kd", "Ks", "Jd", "Td", "8d", "7s", "6h"));
        // One Pair vs High Card
        Assertions.assertTrue(evaluateCards("Kd", "Ks", "Jd", "Td", "7d", "7s", "6h") > evaluateCards("Kd", "Qs", "Jd", "Td", "8d", "7s", "6h"));
    }

    @Test
    public void testFullHouse() {
        // Two Trips is the same as a regular Full House
        Assertions.assertEquals(evaluateCards("Kd", "Ks", "Kh", "7d", "9s", "7s", "7h"), evaluateCards("Kd", "Ks", "Kh", "Td", "7d", "8s", "7h"));
        // Trips and two pair is the same as a regular Full House
        Assertions.assertEquals(evaluateCards("Kd", "Ks", "Kh", "7d", "9s", "7s", "9h"), evaluateCards("Kd", "Ks", "Kh", "Td", "9d", "8s", "9h"));
    }

    @Test
    public void testRoyalFlush() {
        // Royal flush vs Royal flush
        Assertions.assertEquals(evaluateCards("Ah", "Kh", "Qh", "Jh", "Th", "9d", "8d"), evaluateCards("Kd", "Qd", "Jd", "Td", "9d", "Ad", "7h"));
    }

    @Test
    public void cardSuitDoesntMatter() {
        Assertions.assertEquals(evaluateCards("Kh", "Qd", "9s", "8h", "Ts", "3c", "2s"), evaluateCards("Ks", "Qs", "9h", "8d", "Td", "3d", "2c"));
    }

}
