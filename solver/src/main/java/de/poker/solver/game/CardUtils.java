package de.poker.solver.game;

import de.poker.solver.utility.ComparisonUtils;

import java.util.*;

import static de.poker.solver.utility.ComparisonUtils.isXGreaterThanY;

public class CardUtils {
    public static final int NUM_FLOP_COMBINATIONS = 22100;
    private static final Map<Card, Map<Card, Integer>> FLOP_MAP = new HashMap<>();
    private static final Suit[] SUITS_IN_ORDER = new Suit[]{
            Suit.CLUB, Suit.DIAMOND, Suit.HEART, Suit.SPADES
    };

    static {
        Map<Card, Map<Card, List<Card>>> cardMap = new HashMap<>();
        List<List<Card>> cards = new ArrayList<>();
        int counter = 0;
        for (int i=51;i>1;i--) {
            Card cardI = Card.of(i);
            FLOP_MAP.put(cardI, new HashMap<>());
            for (int j=i-1;j>0;j--) {
                Card cardJ = Card.of(j);
                counter += j - 1;
                FLOP_MAP.get(cardI).put(cardJ, j-1);
            }
        }
        System.out.println(cards.size());
    }

    public static int holeCardsToInt(Card card1, Card card2) {
        int numPlaces = card1.value().value() * 2 + 1;
        int startIndex = (int)(0.5 * card1.value().value() * (numPlaces - 1));
        int value = startIndex + card2.value().value() * 2;
        if (card1.suit() == card2.suit()) {
            value++;
        }
        return value;
    }

    public static int flopToInt(Card card1, Card card2, Card card3) {
        return FLOP_MAP.get(card1).get(card2) + card3.toInt();
    }

    public static void normalizeInPlace(List<Card> cards) {
        sortCards(cards);
        normalizeSuit(cards);
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

    private static void sortCards(List<Card> cards) {
        sortHoleCards(cards);
        sortFlop(cards);
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
}
