package de.poker.solver.game;

import de.poker.solver.pluribus.holdem.HoldEmConstants;
import de.poker.solver.utility.ComparisonUtils;

import java.util.*;

import static de.poker.solver.utility.ComparisonUtils.isXGreaterThanY;

public class CardUtils {
    private static final Suit[] SUITS_IN_ORDER = Suit.values();
    private static final long NUM_CARDS = 53;

    public static void normalizeInPlace(List<Card> cards) {
        sortHoleCards(cards);
        normalizeSuit(cards);
        sortFlop(cards);
    }

    private static void normalizeSuit(List<Card> cards) {
        Map<Suit, Suit> suitMapper = new EnumMap<>(Suit.class);
        int suitCounter = 0;

        for (int i=0;i<7;i++) {
            Card card = cards.get(i);
            Suit suit = card.suit();
            if (!suitMapper.containsKey(suit)) {
                suitMapper.put(suit, SUITS_IN_ORDER[suitCounter]);
                suitCounter++;
            }
            cards.set(i, Card.of(card.value(), suitMapper.get(suit)));
        }
    }

    private static void sortFlop(List<Card> cards) {
        Card card1 = cards.get(2);
        Card card2 = cards.get(3);
        Card card3 = cards.get(4);
        boolean card1GreaterThanCard2 = isXGreaterThanY(card1, card2);
        boolean card2GreaterThanCard3 = isXGreaterThanY(card2, card3);
        boolean card1GreaterThanCard3 = isXGreaterThanY(card1, card3);
        if (card1GreaterThanCard2) {
            if (card2GreaterThanCard3) {
                cards.set(2, card1);
                cards.set(3, card2);
                cards.set(4, card3);
            } else if (card1GreaterThanCard3) {
                cards.set(2, card1);
                cards.set(3, card3);
                cards.set(4, card2);
            } else {
                cards.set(2, card3);
                cards.set(3, card1);
                cards.set(4, card2);
            }
        } else if (card2GreaterThanCard3) {
            if (card1GreaterThanCard3) {
                cards.set(2, card2);
                cards.set(3, card1);
                cards.set(4, card3);
            } else {
                cards.set(2, card2);
                cards.set(3, card3);
                cards.set(4, card1);
            }
        } else {
            cards.set(2, card3);
            cards.set(3, card2);
            cards.set(4, card1);
        }
    }

    private static void sortHoleCards(List<Card> cards) {
        Card card1 = cards.get(0);
        Card card2 = cards.get(1);
        if (!ComparisonUtils.isXGreaterThanY(card1, card2)) {
            cards.set(0, card2);
            cards.set(1, card1);
        }
    }

    // TODO test this with bitwise operations
    public static long cardsToLong(List<Card> cards, int depth) {
        long sum = 0;
        long multiplier = 1;
        for (int i=0;i<depth;i++) {
            sum += multiplier * (cards.get(i).toInt() + 1);
            multiplier *= NUM_CARDS;
        }
        return sum;
    }

    public static List<Card> longToCards(long value) {
        List<Card> cards = new ArrayList<>();
        do {
            long cardValue = value % NUM_CARDS;
            value -= cardValue;
            value /= NUM_CARDS;
            cards.add(Card.of((int)cardValue - 1));
        } while (value > 0);
        return cards;
    }
}
