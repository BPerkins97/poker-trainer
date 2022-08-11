package holdem.cap.headsup.preflop;

import de.poker.solver.game.Card;
import de.poker.solver.game.Value;
import de.poker.solver.utility.ComparisonUtils;

public class HandValueCalculator {
    private HandValueCalculator() {}

    public static int calculateHandValue(Card card1, Card card2) {
        Card firstCard = ComparisonUtils.isXGreaterThanY(card1, card2) ? card1 : card2;
        Card secondCard = ComparisonUtils.isXGreaterThanY(card1, card2) ? card2 : card1;

        int sum = secondCard.value().value() + firstCard.value().value() * Value.NUM_VALUES;
        if (firstCard.value().equals(secondCard.value())) {
            sum += Value.NUM_VALUES * Value.NUM_VALUES;
        }
        return sum;
    }
}
