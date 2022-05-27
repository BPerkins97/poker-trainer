package de.poker.trainer.engine;

import java.util.List;

import static java.util.Arrays.asList;

public enum CardValue {
    TWO(0),
    THREE(1),
    FOUR(2),
    FIVE(3),
    SIX(4),
    SEVEN(5),
    EIGHT(6),
    NINE(7),
    TEN(8),
    JACK(9),
    QUEEN(10),
    KING(11),
    ACE(12);

    private final int value;

    CardValue(int value) {
        this.value = value;
    }

    public static List<CardValue> valuesDescending() {
        return asList(ACE, KING, QUEEN, JACK, TEN, NINE, EIGHT, SEVEN, SIX, FIVE, FOUR, THREE, TWO, ACE);
    }

    public int descendingSortValue() {
        return value * -1;
    }

    public int value() {
        return value;
    }
}
