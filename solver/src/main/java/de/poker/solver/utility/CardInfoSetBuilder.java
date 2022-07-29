package de.poker.solver.utility;

import de.poker.solver.game.Card;
import de.poker.solver.game.Suit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.poker.solver.utility.ComparisonUtils.isXGreaterThanY;

public class CardInfoSetBuilder {
    private static final Suit[] SUITS_IN_ORDER = new Suit[]{
            Suit.CLUB, Suit.DIAMOND, Suit.HEART, Suit.SPADES
    };

    private List<Card> cards = new ArrayList<>(7);
    private Map<Suit, Suit> suitMapper = new HashMap<>();
    private int suitCounter = 0;

    private static long hand2Long(List<Card> hand) {
        long sum = 0;
        for (int i=0;i<hand.size();i++) {
            sum += (hand.get(i).toLong() + 1) << (6 * i);
        }
        return sum;
    }

    public CardInfoSetBuilder() {}

    public void appendCard(Card card) {
        if (!suitMapper.containsKey(card.suit())) {
            suitMapper.put(card.suit(), SUITS_IN_ORDER[suitCounter]);
            suitCounter++;
        }
        cards.add(Card.of(card.value(), suitMapper.get(card.suit())));
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

    public static long toInfoSet(List<Card> cards) {
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
        return cardInfoSetBuilder.toLong();
    }

    public List<Card> cards() {
        return cards;
    }

    public long toLong() {
        return hand2Long(cards);
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[cards.size()];
        for (int i=0;i<cards.size();i++) {
            bytes[i] = (byte)cards.get(i).toInt();
        }
        return bytes;
    }
}
