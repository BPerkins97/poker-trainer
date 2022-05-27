package de.poker.trainer.engine;

import static de.poker.trainer.engine.ActionType.CALL;

record Action(Position position, ActionType type, int amount) {

    public static Action fold(Position position) {
        return new Action(position, ActionType.FOLD, 0);
    }

    public static Action bet(Position position, int amount) {
        return new Action(position, ActionType.BET, amount);
    }

    public static Action check(Position position) {
        return new Action(position, ActionType.CHECK, 0);
    }

    public static Action call(Position position) {
        return new Action(position, CALL, 0);
    }
}
