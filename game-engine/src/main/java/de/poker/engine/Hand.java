package de.poker.engine;

import de.poker.engine.utility.CollectionsUtils;

import java.util.*;
import java.util.stream.Collectors;

import static de.poker.engine.Card.Value.*;
import static de.poker.engine.utility.CollectionsUtils.isNotEmpty;

public class Hand {
    private static final Card.Value[][] POSSIBLE_STRAIGHTS;

    static {
        POSSIBLE_STRAIGHTS = new Card.Value[10][];
        POSSIBLE_STRAIGHTS[0] = new Card.Value[]{ACE, KING, QUEEN, JACK, TEN};
        POSSIBLE_STRAIGHTS[1] = new Card.Value[]{KING, QUEEN, JACK, TEN, NINE};
        POSSIBLE_STRAIGHTS[2] = new Card.Value[]{QUEEN, JACK, TEN, NINE, EIGHT};
        POSSIBLE_STRAIGHTS[3] = new Card.Value[]{JACK, TEN, NINE, EIGHT, SEVEN};
        POSSIBLE_STRAIGHTS[4] = new Card.Value[]{TEN, NINE, EIGHT, SEVEN, SIX};
        POSSIBLE_STRAIGHTS[5] = new Card.Value[]{NINE, EIGHT, SEVEN, SIX, FIVE};
        POSSIBLE_STRAIGHTS[6] = new Card.Value[]{EIGHT, SEVEN, SIX, FIVE, FOUR};
        POSSIBLE_STRAIGHTS[7] = new Card.Value[]{SEVEN, SIX, FIVE, FOUR, THREE};
        POSSIBLE_STRAIGHTS[8] = new Card.Value[]{SIX, FIVE, FOUR, THREE, TWO};
        POSSIBLE_STRAIGHTS[9] = new Card.Value[]{FIVE, FOUR, THREE, TWO, ACE};
    }

    private final Rank rank;
    private final List<Card> cards;

    private Hand(Rank rank, List<Card> cards) {
        this.rank = rank;
        this.cards = cards;
    }

    // TODO throw exception when cards are double
    public static Hand of(String card1, String card2, String card3, String card4, String card5, String card6, String card7) {
        List<Card> cards = new ArrayList<>(7);
        cards.add(Card.of(card1));
        cards.add(Card.of(card2));
        cards.add(Card.of(card3));
        cards.add(Card.of(card4));
        cards.add(Card.of(card5));
        cards.add(Card.of(card6));
        cards.add(Card.of(card7));

        cards.sort(Collections.reverseOrder());

        List<List<Card>> flushCards = cards.stream()
                .collect(Collectors.groupingBy(Card::suit))
                .values().stream()
                .filter(list -> list.size() >= 5)
                .toList();

        if (!flushCards.isEmpty()) {
            List<Card> straightFlush = findStraight(
                    flushCards.get(0).stream()
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

        Map<Card.Value, List<Card>> cardsByValue = cards.stream()
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

        if (!flushCards.isEmpty()) {
            List<Card> finalHand = flushCards.get(0).subList(0, 5);
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

    private static List<Card> findStraight(Map<Card.Value, List<Card>> cardByValue) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hand hand = (Hand) o;
        return rank == hand.rank && Objects.equals(cards, hand.cards);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, cards);
    }

    private enum Rank {
        ROYAL_FLUSH("Royal Flush"),
        STRAIGHT_FLUSH("Straight Flush"),
        QUADS("Quads"),
        FULL_HOUSE("Full House"),
        FLUSH("Flush"),
        STRAIGHT("Straight"),
        THREE_OF_A_KIND("Three of a kind"),
        TWO_PAIR("Two Pair"),
        HIGH_CARD("High Card"),
        PAIR("Pair");

        private final String representationString;

        Rank(String representationString) {
            this.representationString = representationString;
        }

        @Override
        public String toString() {
            return representationString;
        }
    }
}
