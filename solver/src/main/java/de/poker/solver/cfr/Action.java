package de.poker.solver.cfr;

public record Action(Type type, double amount) {

    public String forInfoSet() {
        return switch (type) {
            case RAISE -> "r" + amount;
            case CALL -> "c";
            case FOLD -> "f";
        };
    }
    public enum Type {
        FOLD,
        CALL,
        RAISE;
    }
}
