package de.poker.solver.game;

public class Action {
    private static final Action FOLD = new Action('f', 0);
    private static final Action CALL = new Action('c', 0);
    private final char symbol;
    private final int amount;

    public Action(char symbol, int amount) {
        this.symbol = symbol;
        this.amount = amount;
    }

    public static Action fold() {
        return FOLD;
    }

    public static Action call() {
        return CALL;
    }

    public static Action raise(int amount) {
        assert amount > 0;
        return new Action('r', amount);
    }

    public boolean isFold() {
        return symbol == 'f';
    }

    public boolean isCall() {
        return symbol == 'c';
    }

    public int amount() {
        return amount;
    }

    public String asString() {
        return symbol + (amount > 0 ? String.valueOf(amount) : "");
    }
}
