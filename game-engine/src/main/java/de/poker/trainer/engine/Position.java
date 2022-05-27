package de.poker.trainer.engine;

import de.poker.trainer.utility.CollectionUtils;

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

    public static boolean isPlayersTurn(List<Position> playersStillInGame, List<Action> previousActions, Position position) {
        Position nextPosition;
        if (CollectionUtils.isEmpty(previousActions)) {
            nextPosition = SMALL_BLIND;
        } else {
            nextPosition = getNextPosition(previousActions.get(previousActions.size()-1).position());
        }
        while (!playersStillInGame.contains(nextPosition)) {
            nextPosition = getNextPosition(nextPosition);
        }
        return nextPosition.equals(position);
    }

    private static Position getNextPosition(Position position) {
        return switch (position) {
            case SMALL_BLIND -> BIG_BLIND;
            case BIG_BLIND -> LOJACK;
            case LOJACK -> HIJACK;
            case HIJACK -> CUTOFF;
            case CUTOFF -> BUTTON;
            case BUTTON -> SMALL_BLIND;
        };
    }
}
