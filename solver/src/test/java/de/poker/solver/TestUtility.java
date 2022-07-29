package de.poker.solver;

import de.poker.solver.game.Action;
import de.poker.solver.game.Card;
import de.poker.solver.game.HandEvaluator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TestUtility {

    public static Card[] cardsToArray(String... cardStrings) {
        Set<Card> cardSet = Arrays.stream(cardStrings)
                .map(Card::of)
                .collect(Collectors.toSet());
        assert cardSet.size() == cardStrings.length;
        return Arrays.stream(cardStrings)
                .map(Card::of)
                .toArray(Card[]::new);
    }

    public static long evaluateCards(String... cards) {
        assert cards.length == 7;
        return HandEvaluator.of(Arrays.stream(cards)
                .map(Card::of)
                .toList());
    }

    public static String historyToString(byte[] history) {
        String str = "";
        for (int i=0;i<history.length;i++) {
            byte type = history[i];
            int amount = 0;
            if (type == Action.RAISE) {
                i++;
                amount = history[i] + Byte.MAX_VALUE;
            }
            str += Action.of(type, amount).presentation();
        }
        return str;
    }
}
