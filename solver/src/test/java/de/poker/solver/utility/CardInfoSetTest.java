package de.poker.solver.utility;

import de.poker.solver.game.Card;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class CardInfoSetTest {

    @Test
    public void testCases() {
        test("9c5c", "9d", "5d");
        test("9c5d", "9d", "5s");
        test("9c5d", "5d", "9s");
        test("9c5dKhQhJh", "5d", "9s", "Kc", "Qc", "Jc");
        test("9c5dKhQhJh", "5d", "9s", "Kc", "Jc", "Qc");
        test("9c5dKhQhJh", "5d", "9s", "Qc", "Kc", "Jc");
        test("9c5dKhQhJh", "5d", "9s", "Qc", "Jc", "Kc");
        test("9c5dKhQhJh", "5d", "9s", "Jc", "Qc", "Kc");
        test("9c5dKhQhJh", "5d", "9s", "Jc", "Kc", "Qc");
        test("9c5dKhQhJhTd2s", "5d", "9s", "Jc", "Kc", "Qc", "Td", "2h");
        test("9c5dKhQhJh2h", "5d", "9s", "Jc", "Kc", "Qc", "2c");
    }

    private void test(String expected, String... cardStrings) {
        List<Card> cards = Arrays.stream(cardStrings)
                .map(Card::of)
                .toList();
        String actual = CardInfoSetBuilder.toInfoSet(cards);

        Assertions.assertEquals(expected, actual);
    }
}
