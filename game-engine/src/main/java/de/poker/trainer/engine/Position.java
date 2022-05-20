package de.poker.trainer.engine;

import java.util.Arrays;
import java.util.List;

public enum Position {
    SMALL_BLIND,
    BIG_BLIND,
    BUTTON,
    CUTOFF,
    HIJACK,
    LOJACK;

    private static final List<Position> PRE_FLOP_ORDER = Arrays.asList(LOJACK, HIJACK, CUTOFF, BUTTON, SMALL_BLIND, BIG_BLIND);

    public static List<Position> preFlopOrder(List<Position> playersStillInHand) {
        return PRE_FLOP_ORDER.stream()
                .filter(position -> playersStillInHand.contains(position))
                .toList();
    }
}
