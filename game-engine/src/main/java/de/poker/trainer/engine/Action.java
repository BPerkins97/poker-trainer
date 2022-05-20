package de.poker.trainer.engine;

record Action(Position position, ActionType type, int amount) {

    public static Action fold(Position position) {
        return new Action(position, ActionType.FOLD, 0);
    }
}
