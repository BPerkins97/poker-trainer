package de.poker.solver.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class HandEvaluatorTest {
    @Test
    public void testHandRankOrdering() {
        // Royal flush vs Straight flush
        Assertions.assertTrue(of("Ah", "Kh", "Qh", "Jh", "Th", "9d", "8d") > of("Kd", "Qd", "Jd", "Td", "9d", "8h", "7h"));
        // Straight flush vs Four of a kind
        Assertions.assertTrue(of("9h", "Kh", "Qh", "Jh", "Th", "9d", "8d") > of("9h", "Kh", "Qh", "Jh", "9c", "9d", "9s"));
        // Four of a kind vs Full house
        Assertions.assertTrue(of("9h", "Kh", "Qh", "Jh", "9c", "9d", "9s") > of("9h", "Kh", "Qh", "Jh", "Jc", "9d", "9s"));
        // Full house vs flush
        Assertions.assertTrue(of("9h", "Kh", "Qh", "Jh", "Jc", "9d", "9s") > of("Kd", "Qd", "Jd", "Td", "7d", "7s", "7h"));
        // Flush vs Straight
        Assertions.assertTrue(of("9h", "Kh", "Qh", "Jh", "Jc", "9d", "9s") > of("Kd", "Qd", "Jd", "Td", "9s", "7s", "7h"));
        // Straight vs Trips
        Assertions.assertTrue(of("Kd", "Qd", "Jd", "Td", "9s", "7s", "7h") > of("Kd", "Qs", "Jd", "Td", "7d", "7s", "7h"));
        // Trips vs two pair
        Assertions.assertTrue(of("Kd", "Qd", "7d", "Td", "9s", "7s", "7h") > of("Kd", "Ks", "Jd", "Td", "7d", "7s", "6h"));
        // Two pair vs one pair
        Assertions.assertTrue(of("Kd", "Ks", "Jd", "Td", "7d", "7s", "6h") > of("Kd", "Ks", "Jd", "Td", "8d", "7s", "6h"));
        // One Pair vs High Card
        Assertions.assertTrue(of("Kd", "Ks", "Jd", "Td", "7d", "7s", "6h") > of("Kd", "Qs", "Jd", "Td", "8d", "7s", "6h"));
    }

    @Test
    public void testFullHouse() {
        // Two Trips is the same as a regular Full House
        Assertions.assertEquals(of("Kd", "Ks", "Kh", "7d", "9s", "7s", "7h"), of("Kd", "Ks", "Kh", "Td", "7d", "8s", "7h"));
        // Trips and two pair is the same as a regular Full House
        Assertions.assertEquals(of("Kd", "Ks", "Kh", "7d", "9s", "7s", "9h"), of("Kd", "Ks", "Kh", "Td", "9d", "8s", "9h"));
    }

    @Test
    public void testRoyalFlush() {
        // Royal flush vs Royal flush
        Assertions.assertEquals(of("Ah", "Kh", "Qh", "Jh", "Th", "9d", "8d"), of("Kd", "Qd", "Jd", "Td", "9d", "Ad", "7h"));
    }

    @Test
    public void cardSuitDoesntMatter() {
        Assertions.assertEquals(of("Kh", "Qd", "9s", "8h", "Ts", "3c", "2s"), of("Ks", "Qs", "9h", "8d", "Td", "3d", "2c"));
    }

    private long of(String... cards) {
        return HandEvaluator.of(Arrays.stream(cards)
                .map(Card::of)
                .toList());
    }
}
