package de.poker.trainer.engine;

import java.util.Arrays;
import java.util.List;

public enum HandClass {
    HIGH_CARD(0),
    PAIR(1),
    TWO_PAIR(2),
    THREE_OF_A_KIND(3),
    STRAIGHT(4),
    FLUSH(5),
    FULL_HOUSE(6),
    FOUR_OF_A_KIND(7),
    STRAIGHT_FLUSH(8),
    ROYAL_FLUSH(9);

    public static final List<HandClass> ORDERED_DESCENDING = Arrays.asList(ROYAL_FLUSH, STRAIGHT_FLUSH, FOUR_OF_A_KIND, FULL_HOUSE, FLUSH, STRAIGHT, THREE_OF_A_KIND, TWO_PAIR, PAIR, HIGH_CARD);

    private final int value;

    HandClass(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
