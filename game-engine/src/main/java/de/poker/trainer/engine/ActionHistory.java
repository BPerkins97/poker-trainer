package de.poker.trainer.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record ActionHistory(List<Action> preflopActions, List<Action> flopActions, List<Action> turnActions, List<Action> riverActions) {
    public static List<Position> positionsStillInHand(ActionHistory actionHistory) {
        List<Action> actions = getAllActions(actionHistory);

        List<Position> positionsThatFolded = actions.stream()
                .filter(action -> action.type().equals(ActionType.FOLD))
                .map(Action::position)
                .toList();

        List<Position> allPositions = Arrays.asList(Position.values());
        allPositions.removeAll(positionsThatFolded);
        return allPositions;
    }

    private static List<Action> getAllActions(ActionHistory actionHistory) {
        List<Action> actions = new ArrayList<>();
        actions.addAll(actionHistory.preflopActions);
        actions.addAll(actionHistory.flopActions);
        actions.addAll(actionHistory.turnActions);
        actions.addAll(actionHistory.riverActions);
        return actions;
    }
}
