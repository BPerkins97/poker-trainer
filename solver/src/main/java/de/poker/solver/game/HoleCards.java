package de.poker.solver.game;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static de.poker.solver.utility.ComparisonUtils.isXGreaterThanY;

public record HoleCards(Card card1, Card card2) {
    public static HoleCards of(String card1, String card2) {
        Card tempC1 = Card.of(card1);
        Card tempC2 = Card.of(card2);
        if (isXGreaterThanY(tempC1, tempC2)) {
            return new HoleCards(tempC1, tempC2);
        } else {
            return new HoleCards(tempC2, tempC1);
        }
    }

    public List<Card> cards() {
        return Arrays.asList(card1, card2);
    }

    public void appendReducedInfoSet(StringBuilder stringBuilder) {
        if (card1.suit().equals(card2.suit())) {
            if (isXGreaterThanY(card1.value(), card2.value())) {
                stringBuilder
                        .append(card1.value())
                        .append(card2.value());
            } else {
                stringBuilder
                        .append(card2.value())
                        .append(card1.value());
            }
            stringBuilder.append('s');
        } else if (card1.value().equals(card2.value())) {
            String value = card1.value().toString();
            stringBuilder.append(value)
                    .append(value)
                    .append('p');
        } else {
            if (isXGreaterThanY(card1.value(), card2.value())) {
                stringBuilder
                        .append(card1.value())
                        .append(card2.value());
            } else {
                stringBuilder
                        .append(card2.value())
                        .append(card1.value());
            }
            stringBuilder.append('o');
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HoleCards holeCards = (HoleCards) o;
        return Objects.equals(card1, holeCards.card1) && Objects.equals(card2, holeCards.card2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(card1, card2);
    }

    public void appendInfoSet(StringBuilder infoSetBuilder) {
        card1.appendInfoSet(infoSetBuilder);
    }
}
