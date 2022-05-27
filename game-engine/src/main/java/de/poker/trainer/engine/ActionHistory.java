package de.poker.trainer.engine;

import org.springframework.util.CollectionUtils;

import java.util.*;

public record ActionHistory(List<Action> preflopActions, List<Action> flopActions, List<Action> turnActions, List<Action> riverActions) {
    public static List<Position> positionsStillInHand(ActionHistory actionHistory) {
        List<Action> actions = getAllActions(actionHistory);

        List<Position> positionsThatFolded = actions.stream()
                .filter(action -> action.type().equals(ActionType.FOLD))
                .map(Action::position)
                .toList();

        return Arrays.stream(Position.values())
                .filter(position -> !positionsThatFolded.contains(position))
                .toList();
    }

    private static List<Action> getAllActions(ActionHistory actionHistory) {
        List<Action> actions = new ArrayList<>();
        if (Objects.isNull(actionHistory)) {
            return actions;
        }
        if (Objects.nonNull(actionHistory.preflopActions)) {
            actions.addAll(actionHistory.preflopActions);
        }
        if (Objects.nonNull(actionHistory.flopActions)) {
            actions.addAll(actionHistory.flopActions);
        }
        if (Objects.nonNull(actionHistory.turnActions)) {
            actions.addAll(actionHistory.turnActions);
        }
        if (Objects.nonNull(actionHistory.riverActions)) {
            actions.addAll(actionHistory.riverActions);
        }
        return actions;
    }

    public static int potSize(ActionHistory actionHistory) {
        List<Action> actions = getAllActions(actionHistory);
        return actions.stream()
                .map(action -> action.amount())
                .reduce(0, Integer::sum);
    }

    public static ActionHistory blinds() {
        return new ActionHistory(Arrays.asList(new Action(Position.SMALL_BLIND, ActionType.BET, 1), new Action(Position.BIG_BLIND, ActionType.BET, 2)), null, null, null);
    }

    public static List<Position> playersLeftToAct(List<Action> actions, List<Position> playersInGame) {
        int bet = 0;
        List<Position> playersLeftToAct = new ArrayList<>(playersInGame);
        List<Action> raises = actions.stream()
                .filter(action -> action.type().equals(ActionType.RAISE))
                .toList();
        if (!CollectionUtils.isEmpty(raises)) {
            int indexOfLastRaise = actions.indexOf(raises.get(raises.size()-1));
            List<Position> actorsSinceLastRaise = actions.subList(indexOfLastRaise, actions.size())
                    .stream()
                    .map(Action::position)
                    .toList();
            playersLeftToAct.removeAll(actorsSinceLastRaise);
            return playersLeftToAct;
        }

        List<Action> bets = actions.stream()
                .filter(action -> action.type().equals(ActionType.BET))
                .toList();
        if (!CollectionUtils.isEmpty(bets)) {
            int indexOfLastRaise = actions.indexOf(bets.get(bets.size()-1));
            List<Position> actorsSinceLastRaise = actions.subList(indexOfLastRaise, actions.size())
                    .stream()
                    .map(Action::position)
                    .toList();
            playersLeftToAct.removeAll(actorsSinceLastRaise);
            return playersLeftToAct;
        }

        List<Position> actorsSinceLastRaise = actions
                .stream()
                .map(Action::position)
                .toList();
        playersLeftToAct.removeAll(actorsSinceLastRaise);
        return playersLeftToAct;
    }
}
