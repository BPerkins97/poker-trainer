package de.poker.solver.game;

import java.util.Objects;

public record Action(byte type, int amount, String presentation) {
    private static final byte FOLD = 1;
    private static final byte CALL = 2;
    private static final byte RAISE = 4;
    private static final Action ACTION_FOLD = new Action(FOLD, 0, "f");
    private static final Action ACTION_CALL = new Action(CALL, 0, "c");

    public static Action fold() {
        return ACTION_FOLD;
    }

    public static Action call() {
        return ACTION_CALL;
    }

    public static Action raise(int amount) {
        assert amount > 0;
        return new Action(RAISE, amount, "r" + amount);
    }

    public boolean isFold() {
        return type == FOLD;
    }

    public boolean isCall() {
        return type == CALL;
    }

    public int amount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return type == action.type && amount == action.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount);
    }
}
