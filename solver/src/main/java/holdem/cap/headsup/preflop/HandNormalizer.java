package holdem.cap.headsup.preflop;

import de.poker.solver.game.Card;
import de.poker.solver.utility.ComparisonUtils;

public class HandNormalizer {
    public static String normalize(Card card1, Card card2) {
        Card firstCard = ComparisonUtils.isXGreaterThanY(card1, card2) ? card1 : card2;
        Card secondCard = ComparisonUtils.isXGreaterThanY(card1, card2) ? card2 : card1;
        return  "" + firstCard.value() + secondCard.value();
    }
}
