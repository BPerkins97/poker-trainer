package de.poker.solver.game;

import java.util.Objects;

public class Action {
    private static final byte FOLD = 1;
    private static final byte CALL = 2;
    private static final byte RAISE = 4;
    private static final Action ACTION_FOLD = new Action(FOLD, 0);
    private static final Action ACTION_CALL = new Action(CALL, 0);
    private final byte type;
    private final int amount;
    private final String presentation;

    public Action(byte type, int amount) {
        assert type != RAISE || amount > 0;
        this.type = type;
        this.amount = amount;
        this.presentation = switch (type) {
            case FOLD -> "f";
            case CALL -> "c";
            case RAISE -> "r" + amount;
            default -> throw new IllegalArgumentException();
        };
    }

    public static Action fold() {
        return ACTION_FOLD;
    }

    public static Action call() {
        return ACTION_CALL;
    }

    public static Action raise(int amount) {
        assert amount > 0;
        return new Action(RAISE, amount);
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

    public String asString() {
        return presentation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return type == action.type && amount == action.amount && Objects.equals(presentation, action.presentation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount, presentation);
    }
}
