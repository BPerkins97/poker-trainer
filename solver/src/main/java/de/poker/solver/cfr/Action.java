package de.poker.solver.cfr;

import java.util.Objects;

public class Action {
    private static final char RAISE_SYMBOL = 'r';
    private static final char CALL_SYMBOL = 'c';
    private static final char FOLD_SYMBOL = 'f';
    static final Action FOLD = new Action(FOLD_SYMBOL, 0);
    static final Action CALL = new Action(CALL_SYMBOL, 0);

    private final char type;
    final double amount;

    public Action(char type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    public static Action raise(double amount) {
        return new Action(RAISE_SYMBOL, amount);
    }

    public boolean isFold() {
        return type == FOLD_SYMBOL;
    }

    public boolean isCall() {
        return type == CALL_SYMBOL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return Double.compare(action.amount, amount) == 0 && type == action.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount);
    }

    @Override
    public String toString() {
        return "Action{" +
                "type=" + type +
                ", amount=" + amount +
                '}';
    }
}
