package de.poker.solver.game;

import java.util.*;
import java.util.stream.Collectors;

import static de.poker.solver.game.Value.*;
import static de.poker.solver.utility.CollectionUtils.isNotEmpty;

public class Hand implements Comparable<Hand> {
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

    private final Rank rank;
    private final List<Card> cards;
    public final long value;

    private Hand(Rank rank, List<Card> cards) {
        this.rank = rank;
        this.cards = cards;
        long tempSum = 0;
        long multiplier = 1;
        for (int i=4;i>=0;i--) {
            tempSum += cards.get(i).value().value() * multiplier;
            multiplier *= 14;
        }
        tempSum += rank.value() * multiplier;
        value = tempSum;
    }

    public static Hand of(List<Card> cardsInput) {
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

        int clubCount = 0;
        int heartCount = 0;
        int spadeCount = 0;
        int diamondCount = 0;
        for (int i=0;i<7;i++) {
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
            if (isNotEmpty(straightFlush)) {
                if (straightFlush.get(0).value().equals(POSSIBLE_STRAIGHTS[0][0])) {
                    return new Hand(Rank.ROYAL_FLUSH, straightFlush);
                } else {
                    return new Hand(Rank.STRAIGHT_FLUSH, straightFlush);
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
            return new Hand(Rank.QUADS, finalHand);
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
            return new Hand(Rank.FULL_HOUSE, finalHand);
        }

        if (trips.size() == 2) {
            List<Card> finalHand = trips.get(0);
            finalHand.addAll(trips.get(1));
            finalHand.sort(Collections.reverseOrder());
            return new Hand(Rank.FULL_HOUSE, finalHand.subList(0, 5));
        }

        if (trips.size() == 1 && pairs.size() == 2) {
            List<Card> finalHand = trips.get(0);
            List<Card> subFinalHand = pairs.get(0);
            subFinalHand.addAll(pairs.get(1));
            subFinalHand.sort(Collections.reverseOrder());
            finalHand.addAll(subFinalHand.subList(0, 2));
            return new Hand(Rank.FULL_HOUSE, finalHand);
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
            return new Hand(Rank.FLUSH, finalHand);
        }

        List<Card> straight = findStraight(cardsByValue);

        if (!straight.isEmpty()) {
            return new Hand(Rank.STRAIGHT, straight);
        }

        if (trips.size() == 1) {
            List<Card> finalHand = trips.get(0);
            addNCardsIfNotInCollection(cards, 2, finalHand);
            return new Hand(Rank.THREE_OF_A_KIND, finalHand);
        }

        if (pairs.size() == 2) {
            List<Card> finalHand = pairs.get(0);
            finalHand.addAll(pairs.get(1));
            finalHand.sort(Collections.reverseOrder());
            addNCardsIfNotInCollection(cards, 1, finalHand);
            return new Hand(Rank.TWO_PAIR, finalHand);
        }

        if (pairs.size() == 1) {
            List<Card> finalHand = pairs.get(0);
            addNCardsIfNotInCollection(cards, 3, finalHand);
            return new Hand(Rank.PAIR, finalHand);
        }
        return new Hand(Rank.HIGH_CARD, cards.subList(0, 5));
    }
    public static Hand of(String... cardsInput) {
        return of(Arrays.stream(cardsInput)
                .map(Card::of)
                .toList());
    }


    private static List<Card> findStraight(Map<Value, List<Card>> cardByValue) {
        for (int j = 0; j < 10; j++) {
            if (
                    isNotEmpty(cardByValue.get(POSSIBLE_STRAIGHTS[j][0])) &&
                            isNotEmpty(cardByValue.get(POSSIBLE_STRAIGHTS[j][1])) &&
                            isNotEmpty(cardByValue.get(POSSIBLE_STRAIGHTS[j][2])) &&
                            isNotEmpty(cardByValue.get(POSSIBLE_STRAIGHTS[j][3])) &&
                            isNotEmpty(cardByValue.get(POSSIBLE_STRAIGHTS[j][4]))
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

    public String toString() {
        return rank + ": " + cards.stream().map(Card::toString).collect(Collectors.joining("-"));
    }

    @Override
    public int compareTo(Hand hand) {
        return Long.compare(this.value, hand.value);
    }

    private enum Rank {
        ROYAL_FLUSH(9,"Royal Flush"),
        STRAIGHT_FLUSH(8,"Straight Flush"),
        QUADS(7,"Quads"),
        FULL_HOUSE(6,"Full House"),
        FLUSH(5,"Flush"),
        STRAIGHT(4,"Straight"),
        THREE_OF_A_KIND(3,"Three of a kind"),
        TWO_PAIR(2,"Two Pair"),
        HIGH_CARD(0,"High Card"),
        PAIR(1,"Pair");

        private final String representationString;
        private final int value;
        Rank(int value, String representationString) {
            this.value = value;
            this.representationString = representationString;
        }

        public int value() {
            return value;
        }

        @Override
        public String toString() {
            return representationString;
        }
    }
}