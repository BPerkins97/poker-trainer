package de.poker.solver.utility;

import de.poker.solver.game.Card;
import de.poker.solver.game.Suit;

import java.util.*;

import static de.poker.solver.utility.ComparisonUtils.isXGreaterThanY;

public class CardInfoSetBuilder {
    private static final Suit[] SUITS_IN_ORDER = new Suit[]{
            Suit.CLUB, Suit.DIAMOND, Suit.HEART, Suit.SPADES
    };

    private StringBuilder sb = new StringBuilder(15);
    private Map<Suit, Suit> suitMapper = new HashMap<>();
    private int suitCounter = 0;

    public CardInfoSetBuilder() {}

    public void appendCard(Card card) {
        if (!suitMapper.containsKey(card.suit())) {
            suitMapper.put(card.suit(), SUITS_IN_ORDER[suitCounter]);
            suitCounter++;
        }
        sb.append(card.value());
        sb.append(suitMapper.get(card.suit()));
    }

    public void appendHoleCards(Card card1, Card card2) {
        if (isXGreaterThanY(card1, card2)) {
            appendCard(card1);
            appendCard(card2);
        } else {
            appendCard(card2);
            appendCard(card1);
        }
    }

    public void appendFlop(Card card1, Card card2, Card card3) {
        boolean card1GreaterThanCard2 = isXGreaterThanY(card1, card2);
        boolean card2GreaterThanCard3 = isXGreaterThanY(card2, card3);
        boolean card1GreaterThanCard3 = isXGreaterThanY(card1, card3);
        if (card1GreaterThanCard2) {
            if (card2GreaterThanCard3) {
                appendCard(card1);
                appendCard(card2);
                appendCard(card3);
            } else if (card1GreaterThanCard3) {
                appendCard(card1);
                appendCard(card3);
                appendCard(card2);
            } else {
                appendCard(card3);
                appendCard(card1);
                appendCard(card2);
            }
        } else if (card2GreaterThanCard3) {
            if (card1GreaterThanCard3) {
                appendCard(card2);
                appendCard(card1);
                appendCard(card3);
            } else {
                appendCard(card2);
                appendCard(card3);
                appendCard(card1);
            }
        } else {
            appendCard(card3);
            appendCard(card2);
            appendCard(card1);
        }
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    public static String toInfoSet(List<Card> cards) {
        CardInfoSetBuilder cardInfoSetBuilder = new CardInfoSetBuilder();
        cardInfoSetBuilder.appendHoleCards(cards.get(0), cards.get(1));
        if (cards.size() >= 5) {
            cardInfoSetBuilder.appendFlop(cards.get(2), cards.get(3), cards.get(4));
        }
        if (cards.size() >= 6) {
            cardInfoSetBuilder.appendCard(cards.get(5));
        }
        if (cards.size() >= 7) {
            cardInfoSetBuilder.appendCard(cards.get(6));
        }
        return cardInfoSetBuilder.toString();
    }
}
