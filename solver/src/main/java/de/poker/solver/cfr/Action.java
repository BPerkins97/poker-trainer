package de.poker.solver.cfr;

public record Action(Type type, double amount) {
    public enum Type {
        FOLD,
        CALL,
        RAISE;
    }
}
