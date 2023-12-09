package de.poker.trainer.solver.montecarlocfr.game;

import de.poker.trainer.solver.montecarlocfr.utility.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static de.poker.trainer.solver.montecarlocfr.game.Value.*;

public class HandEvaluator {
    private HandEvaluator() {}

    private static final Value[][] POSSIBLE_STRAIGHTS;

    static {
        POSSIBLE_STRAIGHTS = new Value[10][];
        POSSIBLE_STRAIGHTS[0] = new Value[]{ACE, KING, QUEEN, JACK, TEN};
        POSSIBLE_STRAIGHTS[1] = new Value[]{KING, QUEEN, JACK, TEN, NINE};
        POSSIBLE_STRAIGHTS[2] = new Value[]{QUEEN, JACK, TEN, NINE, EIGHT};
        POSSIBLE_STRAIGHTS[3] = new Value[]{JACK, TEN, NINE, EIGHT, SEVEN};
        POSSIBLE_STRAIGHTS[4] = new Value[]{TEN, NINE, EIGHT, SEVEN, SIX};
        POSSIBLE_STRAIGHTS[5] = new Value[]{NINE, EIGHT, SEVEN, SIX, FIVE};
        POSSIBLE_STRAIGHTS[6] = new Value[]{EIGHT, SEVEN, SIX, FIVE, FOUR};
        POSSIBLE_STRAIGHTS[7] = new Value[]{SEVEN, SIX, FIVE, FOUR, THREE};
        POSSIBLE_STRAIGHTS[8] = new Value[]{SIX, FIVE, FOUR, THREE, TWO};
        POSSIBLE_STRAIGHTS[9] = new Value[]{FIVE, FOUR, THREE, TWO, ACE};
    }

    public static long of(List<Card> cardsInput) {
        assert cardsInput != null : "Cant instantiate a Hand from null";
        assert cardsInput.size() == 7 : "Expected 7 cards but only got " + cardsInput.size();

        List<Card> cards = new ArrayList<>(7);
        cards.add(cardsInput.get(0));
        cards.add(cardsInput.get(1));
        cards.add(cardsInput.get(2));
        cards.add(cardsInput.get(3));
        cards.add(cardsInput.get(4));
        cards.add(cardsInput.get(5));
        cards.add(cardsInput.get(6));

        cards.sort(Collections.reverseOrder());

        // We can do this in one int and work with bitmask
        int clubCount = 0;
        int heartCount = 0;
        int spadeCount = 0;
        int diamondCount = 0;
        for (int i = 0; i < 7; i++) {
            switch (cards.get(i).suit()) {
                case CLUB:
                    clubCount++;
                    break;
                case HEART:
                    heartCount++;
                    break;
                case SPADES:
                    spadeCount++;
                    break;
                case DIAMOND:
                    diamondCount++;
                    break;
            }
        }
        boolean isFlush = clubCount >= 5 || heartCount >= 5 || spadeCount >= 5 || diamondCount >= 5;


        if (isFlush) {
            Suit flushSuit;
            if (clubCount >= 5) {
                flushSuit = Suit.CLUB;
            } else if (heartCount >= 5) {
                flushSuit = Suit.HEART;
            } else if (spadeCount >= 5) {
                flushSuit = Suit.SPADES;
            } else {
                flushSuit = Suit.DIAMOND;
            }
            List<Card> flushCards = cards.stream()
                    .filter(card -> card.suit().equals(flushSuit))
                    .toList();
            List<Card> straightFlush = findStraight(
                    flushCards.stream()
                            .collect(Collectors.groupingBy(Card::value))
            );
            if (CollectionUtils.isNotEmpty(straightFlush)) {
                if (straightFlush.get(0).value().equals(POSSIBLE_STRAIGHTS[0][0])) {
                    return toLong(straightFlush, Rank.ROYAL_FLUSH);
                } else {
                    return toLong(straightFlush, Rank.STRAIGHT_FLUSH);
                }
            }
        }

        Map<Value, List<Card>> cardsByValue = cards.stream()
                .collect(Collectors.groupingBy(Card::value));

        List<List<Card>> quads = cardsByValue
                .values().stream()
                .filter(cardList -> cardList.size() == 4)
                .toList();

        if (quads.size() == 1) {
            List<Card> finalHand = quads.get(0);
            addNCardsIfNotInCollection(cards, 1, finalHand);
            return toLong(finalHand, Rank.QUADS);
        }

        List<List<Card>> pairs = cardsByValue
                .values().stream()
                .filter(cardList -> cardList.size() == 2)
                .toList();
        List<List<Card>> trips = cardsByValue
                .values().stream()
                .filter(cardList -> cardList.size() == 3)
                .toList();

        if (trips.size() == 1 && pairs.size() == 1) {
            List<Card> finalHand = trips.get(0);
            finalHand.addAll(pairs.get(0));
            return toLong(finalHand, Rank.FULL_HOUSE);
        }

        if (trips.size() == 2) {
            List<Card> finalHand = trips.get(0);
            finalHand.addAll(trips.get(1));
            finalHand.sort(Collections.reverseOrder());
            return toLong(finalHand.subList(0, 5), Rank.FULL_HOUSE);
        }

        if (trips.size() == 1 && pairs.size() == 2) {
            List<Card> finalHand = trips.get(0);
            List<Card> subFinalHand = pairs.get(0);
            subFinalHand.addAll(pairs.get(1));
            subFinalHand.sort(Collections.reverseOrder());
            finalHand.addAll(subFinalHand.subList(0, 2));
            return toLong(finalHand, Rank.FULL_HOUSE);
        }

        if (isFlush) {
            Suit flushSuit;
            if (clubCount >= 5) {
                flushSuit = Suit.CLUB;
            } else if (heartCount >= 5) {
                flushSuit = Suit.HEART;
            } else if (spadeCount >= 5) {
                flushSuit = Suit.SPADES;
            } else {
                flushSuit = Suit.DIAMOND;
            }
            List<Card> flushCards = cards.stream()
                    .filter(card -> card.suit().equals(flushSuit))
                    .collect(Collectors.toList());
            List<Card> finalHand = flushCards.subList(0, 5);
            finalHand.sort(Collections.reverseOrder());
            return toLong(finalHand, Rank.FLUSH);
        }

        List<Card> straight = findStraight(cardsByValue);

        if (!straight.isEmpty()) {
            return toLong(straight, Rank.STRAIGHT);
        }

        if (trips.size() == 1) {
            List<Card> finalHand = trips.get(0);
            addNCardsIfNotInCollection(cards, 2, finalHand);
            return toLong(finalHand, Rank.THREE_OF_A_KIND);
        }

        if (pairs.size() == 2) {
            List<Card> finalHand = pairs.get(0);
            finalHand.addAll(pairs.get(1));
            finalHand.sort(Collections.reverseOrder());
            addNCardsIfNotInCollection(cards, 1, finalHand);
            return toLong(finalHand, Rank.TWO_PAIR);
        }

        if (pairs.size() == 1) {
            List<Card> finalHand = pairs.get(0);
            addNCardsIfNotInCollection(cards, 3, finalHand);
            return toLong(finalHand, Rank.PAIR);
        }
        return toLong(cards.subList(0, 5), Rank.HIGH_CARD);
    }

    private static long toLong(List<Card> cards, Rank rank) {
        long tempSum = 0;
        long multiplier = 1;
        for (int i = 4; i >= 0; i--) {
            tempSum += cards.get(i).value().value() * multiplier;
            multiplier *= 14;
        }
        tempSum += rank.value() * multiplier;
        return tempSum;
    }

    private static List<Card> findStraight(Map<Value, List<Card>> cardByValue) {
        for (int j = 0; j < 10; j++) {
            if (
                    CollectionUtils.isNotEmpty(cardByValue.get(POSSIBLE_STRAIGHTS[j][0])) &&
                            CollectionUtils.isNotEmpty(cardByValue.get(POSSIBLE_STRAIGHTS[j][1])) &&
                            CollectionUtils.isNotEmpty(cardByValue.get(POSSIBLE_STRAIGHTS[j][2])) &&
                            CollectionUtils.isNotEmpty(cardByValue.get(POSSIBLE_STRAIGHTS[j][3])) &&
                            CollectionUtils.isNotEmpty(cardByValue.get(POSSIBLE_STRAIGHTS[j][4]))
            ) {
                List<Card> cards1 = new LinkedList<>();
                cards1.add(cardByValue.get(POSSIBLE_STRAIGHTS[j][0]).get(0));
                cards1.add(cardByValue.get(POSSIBLE_STRAIGHTS[j][1]).get(0));
                cards1.add(cardByValue.get(POSSIBLE_STRAIGHTS[j][2]).get(0));
                cards1.add(cardByValue.get(POSSIBLE_STRAIGHTS[j][3]).get(0));
                cards1.add(cardByValue.get(POSSIBLE_STRAIGHTS[j][4]).get(0));
                return cards1;
            }
        }
        return new ArrayList<>();
    }

    private static void addNCardsIfNotInCollection(List<Card> actualCollection, int nCards, List<Card> alreadyDrawnCards) {
        int numCards = actualCollection.size();
        List<Card> result = new ArrayList<>(nCards);
        int resultCounter = 0;
        for (int i = 0; i < numCards && resultCounter < nCards; i++) {
            if (!alreadyDrawnCards.contains(actualCollection.get(i))) {
                result.add(actualCollection.get(i));
                resultCounter++;
            }
        }
        alreadyDrawnCards.addAll(result);
    }

    private enum Rank {
        ROYAL_FLUSH(9),
        STRAIGHT_FLUSH(8),
        QUADS(7),
        FULL_HOUSE(6),
        FLUSH(5),
        STRAIGHT(4),
        THREE_OF_A_KIND(3),
        TWO_PAIR(2),
        HIGH_CARD(0),
        PAIR(1);

        private final int value;

        Rank(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }
}
