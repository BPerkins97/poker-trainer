package de.poker.trainer.games.nlhe;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HandRankingTest {

    private static Stream<Arguments> handRankings() {
        return Stream.of(
                Arguments.of(cards("Jd", "Qd", "Td", "2c", "Kd", "5h", "Ad"), HandRank.ROYAL_FLUSH, cards("Ad", "Kd", "Qd", "Jd", "Td")),
                Arguments.of(cards("Jd", "Qd", "Td", "2c", "Kd", "5h", "9d"), HandRank.STRAIGHT_FLUSH, cards("Kd", "Qd", "Jd", "Td", "9d")),
                Arguments.of(cards("Jd", "Ah", "3h", "2h", "Kd", "5h", "4h"), HandRank.STRAIGHT_FLUSH, cards("5h", "4h", "3h", "2h", "Ah")),
                Arguments.of(cards("2s", "2d", "2h", "2c", "4h", "5h", "7d"), HandRank.FOUR_OF_A_KIND, cards("2c", "2d", "2h", "2s", "7d")),
                Arguments.of(cards("2s", "2d", "2h", "4c", "4h", "5h", "7d"), HandRank.FULL_HOUSE, cards("2d", "2h", "2s", "4c", "4h")),
                Arguments.of(cards("2s", "2d", "2h", "4c", "4h", "4d", "7d"), HandRank.FULL_HOUSE, cards("4c", "4d", "4h", "2d", "2h")),
                Arguments.of(cards("2s", "2d", "2h", "4c", "4h", "7s", "7d"), HandRank.FULL_HOUSE, cards("2d", "2h", "2s", "7d", "7s")),
                Arguments.of(cards("Kh", "2d", "2h", "Th", "4h", "5h", "7d"), HandRank.FLUSH, cards("Kh", "Th", "5h", "4h", "2h")),
                Arguments.of(cards("Kh", "Qd", "Jc", "Th", "4h", "9c", "7d"), HandRank.STRAIGHT, cards("Kh", "Qd", "Jc", "Th", "9c")),
                Arguments.of(cards("Jd", "Ah", "3h", "2h", "Kd", "5h", "4d"), HandRank.STRAIGHT, cards("5h", "4d", "3h", "2h", "Ah")),
                Arguments.of(cards("Kh", "Qd", "Jc", "Jh", "4h", "Js", "7d"), HandRank.THREE_OF_A_KIND, cards("Jc", "Jh", "Js", "Kh", "Qd")),
                Arguments.of(cards("Kh", "Qd", "Qc", "Jh", "4h", "Js", "7d"), HandRank.TWO_PAIR, cards("Qc", "Qd", "Jh", "Js", "Kh")),
                Arguments.of(cards("Kh", "Qd", "6d", "Jh", "4h", "Js", "7d"), HandRank.PAIR, cards("Jh", "Js", "Kh", "Qd", "7d")),
                Arguments.of(cards("Kh", "Qd", "6d", "Jh", "4h", "2s", "7d"), HandRank.HIGH_CARD, cards("Kh", "Qd", "Jh", "7d", "6d"))
        );
    }

    @ParameterizedTest
    @MethodSource("handRankings")
    public void handRankingTest(List<Card> handCards, HandRank rank, List<Card> rankedCards) {
        RankingResult result = HandRanker.rank(handCards);
        assertEquals(rank, result.handRank());
        assertEquals(rankedCards, result.cards());
    }

    private static List<Card> cards(String... cardStrings) {
        return Arrays.stream(cardStrings)
                .map(cardStr -> new Card(rank(cardStr.substring(0, 1)), suit(cardStr.substring(1))))
                .collect(Collectors.toList());
    }

    private static CardRank rank(String rankStr) {
        return Arrays.stream(CardRank.values())
                .filter(rank -> rank.representation.equals(rankStr))
                .findFirst().orElseThrow();
    }

    private static CardSuit suit(String suitStr) {
        return Arrays.stream(CardSuit.values())
                .filter(suit -> suit.representation.equals(suitStr))
                .findFirst().orElseThrow();
    }
}
