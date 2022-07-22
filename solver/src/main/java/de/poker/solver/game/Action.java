package de.poker.solver.game;

import java.util.Objects;

public record Action(byte type, int amount, String presentation) {
    private static final byte FOLD = 1;
    private static final byte CALL = 2;
    private static final byte RAISE = 4;
    private static final Action ACTION_FOLD = new Action(FOLD, 0, "f");
    private static final Action ACTION_CALL = new Action(CALL, 0, "c");

    private static final Action[] ACTION_RAISE = new Action[Constants.STARTING_STACK_SIZE];

    static {
        for (int i=1;i<=Constants.STARTING_STACK_SIZE;i++) {
            ACTION_RAISE[i-1] = new Action(RAISE, i, "r" + i);
        }
    }

    public static Action fold() {
        return ACTION_FOLD;
    }

    public static Action call() {
        return ACTION_CALL;
    }

    public static Action raise(int amount) {
        assert amount > 0;
        return ACTION_RAISE[amount-1];
    }

    public static Action of(String action) {
        if (action.startsWith("f")) {
            return Action.fold();
        }
        if (action.startsWith("c")) {
            return Action.call();
        }
        if (action.startsWith("r")) {
            return Action.raise(Integer.parseInt(action.substring(1)));
        }
        throw new IllegalArgumentException();
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
