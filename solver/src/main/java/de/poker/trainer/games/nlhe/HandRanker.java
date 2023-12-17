package de.poker.trainer.games.nlhe;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HandRanker {
    public static RankingResult rank(List<Card> cards) {
        RankingResult rank = getRoyalFlush(cards);
        if (rank != null) {
            return rank;
        }
        rank = getStraightFlush(cards);
        if (rank != null) {
            return rank;
        }
        rank = getFourOfAKind(cards);
        if (rank != null) {
            return rank;
        }
        rank = getFullHouse(cards);
        if (rank != null) {
            return rank;
        }
        rank = getFlush(cards);
        if (rank != null) {
            return rank;
        }
        rank = getStraight(cards);
        if (rank != null) {
            return rank;
        }
        rank = getThreeOfAKind(cards);
        if (rank != null) {
            return rank;
        }
        rank = getTwoPair(cards);
        if (rank != null) {
            return rank;
        }
        rank = getPair(cards);
        if (rank != null) {
            return rank;
        }
        return getHighCard(cards);
    }

    private static RankingResult getHighCard(List<Card> cards) {
        List<Card> handCards = cards.stream()
                .sorted(Comparator.comparing(Card::value).reversed())
                .limit(5)
                .toList();
        return new RankingResult(HandRank.HIGH_CARD, handCards);
    }

    private static RankingResult getPair(List<Card> cards) {
        List<Card> pairs = cards.stream()
                .collect(Collectors.groupingBy(Card::rank))
                .values().stream()
                .filter(cardList -> cardList.size() == 2)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Card::value).reversed())
                .limit(5)
                .toList();
        // At least one pair / 1 * 2 = 2
        if (pairs.size() >= 2) {
            int missingCards = 5 - pairs.size();
            List<Card> handCards = Stream.concat(
                            pairs.stream(),
                            cards.stream()
                                    .filter(c -> !pairs.contains(c))
                                    .sorted(Comparator.comparing(Card::value).reversed())
                                    .limit(missingCards)
                    )
                    .toList();
            return new RankingResult(HandRank.PAIR, handCards);
        }
        return null;
    }

    private static RankingResult getTwoPair(List<Card> cards) {
        List<Card> pairs = cards.stream()
                .collect(Collectors.groupingBy(Card::rank))
                .values().stream()
                .filter(cardList -> cardList.size() == 2)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Card::value).reversed())
                .limit(5)
                .toList();
        // At least two pairs / 2 * 2 = 4
        if (pairs.size() >= 4) {
            int missingCards = 5 - pairs.size();
            List<Card> handCards = Stream.concat(
                    pairs.stream(),
                    cards.stream()
                            .filter(c -> !pairs.contains(c))
                            .sorted(Comparator.comparing(Card::value).reversed())
                            .limit(missingCards)
            )
                    .toList();
            return new RankingResult(HandRank.TWO_PAIR, handCards);
        }
        return null;
    }

    private static RankingResult getThreeOfAKind(List<Card> cards) {
        Map<CardRank, List<Card>> valueCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::rank));
        Optional<CardRank> threeOfAKindRank = valueCounts.entrySet().stream()
                .filter(e -> e.getValue().size() == 3)
                .map(Map.Entry::getKey)
                .findFirst();
        if (threeOfAKindRank.isPresent()) {
            List<Card> rankedCards = Stream.concat(
                    cards.stream()
                            .filter(c -> c.rank().equals(threeOfAKindRank.get()))
                            .sorted(Comparator.comparing(Card::value).reversed()),
                    cards.stream()
                            .filter(c -> !c.rank().equals(threeOfAKindRank.get()))
                            .sorted(Comparator.comparing(Card::value).reversed())
                            .limit(2)
            ).toList();
            return new RankingResult(HandRank.THREE_OF_A_KIND, rankedCards);
        }
        return null;
    }

    private static RankingResult getStraight(List<Card> cards) {
        List<Card> potentialStraightCards = cards.stream()
                .sorted(Comparator.comparing(Card::value).reversed())
                .toList();
        int rank = potentialStraightCards.get(0).rank().value();
        List<Card> straightCards = new ArrayList<>(5);
        straightCards.add(potentialStraightCards.get(0));
        for (int i = 1; i < potentialStraightCards.size() && straightCards.size() < 5; i++) {
            int nextRank = rank - 1;
            if (nextRank == potentialStraightCards.get(i).rank().value()) {
                straightCards.add(potentialStraightCards.get(i));
            } else if (rank != potentialStraightCards.get(i).rank().value()) {
                if (i > potentialStraightCards.size() - 4) {
                    return null;
                }
                straightCards.clear();
                straightCards.add(potentialStraightCards.get(i));
            }
            rank = potentialStraightCards.get(i).rank().value();
        }
        if (straightCards.size() == 4 && rank - 1 == -1 && potentialStraightCards.get(0).rank().equals(CardRank.ACE)) {
            straightCards.add(potentialStraightCards.get(0));
        }
        if (straightCards.size() >= 5) {
            return new RankingResult(HandRank.STRAIGHT, straightCards);
        }
        return null;
    }

    private static RankingResult getFlush(List<Card> cards) {
        List<Card> flushCards = cards.stream()
                .collect(Collectors.groupingBy(Card::suit))
                .values().stream()
                .filter(cardList -> cardList.size() >= 5)
                .findFirst().orElse(null);
        if (flushCards == null) {
            return null;
        }
        flushCards = flushCards.stream()
                .sorted(Comparator.comparing(Card::value).reversed())
                .limit(5)
                .toList();
        return new RankingResult(HandRank.FLUSH, flushCards);
    }

    // TODO when sorting for straight, we can shortcut if the first card is a low card (e.g. a 3)
    // not possible for straight flush
    private static RankingResult getStraightFlush(List<Card> cards) {
        CardSuit flushSuit = cards.stream()
                .collect(Collectors.groupingBy(Card::suit))
                .entrySet().stream()
                .filter(e -> e.getValue().size() >= 5)
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
        if (flushSuit == null) {
            return null;
        }
        List<Card> potentialStraightCards = cards.stream()
                .filter(c -> c.suit().equals(flushSuit))
                .sorted(Comparator.comparing(Card::value).reversed())
                .collect(Collectors.toList());
        int rank = potentialStraightCards.get(0).rank().value();
        int straight = 1;
        int straightStart = 0;
        for (int i = 1; i < potentialStraightCards.size() && straight < 5; i++) {
            int nextRank = rank - 1;
            if (nextRank == potentialStraightCards.get(i).rank().value()) {
                straight++;
            } else {
                if (i > potentialStraightCards.size() - 4) {
                    return null;
                }
                straightStart = i;
                straight = 1;
            }
            rank = potentialStraightCards.get(i).rank().value();
        }
        if (straight >= 5) {
            return new RankingResult(HandRank.STRAIGHT_FLUSH, potentialStraightCards.subList(straightStart, straightStart + 5));
        } else if (straight == 4 && rank - 1 == -1 && potentialStraightCards.get(0).rank().equals(CardRank.ACE)) {
            potentialStraightCards.add(potentialStraightCards.get(0));
            return new RankingResult(HandRank.STRAIGHT_FLUSH, potentialStraightCards.subList(straightStart, straightStart + 5));
        }
        return null;
    }

    private static RankingResult getRoyalFlush(List<Card> cards) {
        CardSuit flushSuit = cards.stream()
                .collect(Collectors.groupingBy(Card::suit))
                .entrySet().stream()
                .filter(e -> e.getValue().size() >= 5)
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
        if (flushSuit == null) {
            return null;
        }
        List<Card> potentialRoyalFlushCards = cards.stream()
                .filter(c -> c.suit().equals(flushSuit))
                .sorted(Comparator.comparing(Card::value).reversed())
                .toList();
        if (
                potentialRoyalFlushCards.get(0).rank().equals(CardRank.ACE)
                        && potentialRoyalFlushCards.get(1).rank().equals(CardRank.KING)
                        && potentialRoyalFlushCards.get(2).rank().equals(CardRank.QUEEN)
                        && potentialRoyalFlushCards.get(3).rank().equals(CardRank.JACK)
                        && potentialRoyalFlushCards.get(4).rank().equals(CardRank.TEN)
        ) {
            return new RankingResult(HandRank.ROYAL_FLUSH, potentialRoyalFlushCards.subList(0, 5));
        }
        return null;
    }

    private static RankingResult getFullHouse(List<Card> cards) {
        Map<CardRank, List<Card>> valueCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::rank));
        List<Card> threeOfAKindCards = valueCounts
                .values().stream()
                .filter(c -> c.size() == 3)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Card::value).reversed())
                .limit(5)
                .toList();
        if (threeOfAKindCards.size() < 3) {
            return null;
        }
        if (threeOfAKindCards.size() == 5) {
            return new RankingResult(HandRank.FULL_HOUSE, threeOfAKindCards);
        }
        List<Card> pairCards = valueCounts.values().stream()
                .filter(c -> c.size() == 2)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Card::value).reversed())
                .limit(2)
                .toList();
        if (pairCards.size() == 2) {
            List<Card> rankedCards = Stream.concat(
                    threeOfAKindCards.stream(),
                    pairCards.stream()
            ).toList();
            return new RankingResult(HandRank.FULL_HOUSE, rankedCards);
        }
        return null;
    }

    private static RankingResult getFourOfAKind(List<Card> cards) {
        ArrayList<Card> mutableCards = new ArrayList<>(cards);
        Map<CardRank, List<Card>> valueCounts = cards.stream()
                .collect(Collectors.groupingBy(Card::rank));
        Optional<CardRank> fourOfAKindRank = valueCounts.entrySet().stream()
                .filter(e -> e.getValue().size() == 4)
                .map(Map.Entry::getKey)
                .findFirst();
        if (fourOfAKindRank.isPresent()) {
            List<Card> fourOfAKindCards = new ArrayList<>(cards.stream()
                    .filter(c -> c.rank().equals(fourOfAKindRank.get()))
                    .sorted(Comparator.comparing(Card::value).reversed())
                    .toList());
            mutableCards.removeAll(fourOfAKindCards);
            mutableCards.sort(Comparator.comparing(Card::value).reversed());
            fourOfAKindCards.add(mutableCards.get(0));
            return new RankingResult(HandRank.FOUR_OF_A_KIND, fourOfAKindCards);
        }
        return null;
    }
}
