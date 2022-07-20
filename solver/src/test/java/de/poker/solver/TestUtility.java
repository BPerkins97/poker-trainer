package de.poker.solver;

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
}
