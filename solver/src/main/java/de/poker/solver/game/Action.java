package de.poker.solver.game;

import java.util.Objects;

public record Action(byte type, short amount, String presentation) {
    public static final byte FOLD = 1;
    public static final byte CALL = 2;
    public static final byte RAISE = 4;
    private static final Action ACTION_FOLD = new Action(FOLD, (short) 0, "f");
    private static final Action ACTION_CALL = new Action(CALL, (short) 0, "c");

    private static final Action[] ACTION_RAISE = new Action[Constants.STARTING_STACK_SIZE];

    static {
        for (short i=1;i<=Constants.STARTING_STACK_SIZE;i++) {
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

    public static Action of(byte actionId, int amount) {
        return switch (actionId) {
            case FOLD -> Action.fold();
            case CALL -> Action.call();
            case RAISE -> Action.raise(amount);
            default -> throw new IllegalArgumentException();
        };
    }

    public boolean isFold() {
        return type == FOLD;
    }

    public boolean isCall() {
        return type == CALL;
    }

    public short amount() {
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
