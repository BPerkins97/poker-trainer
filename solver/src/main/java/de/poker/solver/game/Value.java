package de.poker.solver.game;

public enum Value {
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT( "8"),
    NINE( "9"),
    TEN("T"),
    JACK("J"),
    QUEEN("Q"),
    KING("K"),
    ACE("A");

    public static final int NUM_VALUES = 13;
    private static final Value[] VALUES = new Value[13];

    static {
        for (Value value : Value.values()) {
            VALUES[value.ordinal()] = value;
        }
    }
    final String presentation;

    Value(String presentation) {
        this.presentation = presentation;
    }

    public static Value parse(String symbol) {
        return switch (symbol) {
            case "2" -> TWO;
            case "3" -> THREE;
            case "4" -> FOUR;
            case "5" -> FIVE;
            case "6" -> SIX;
            case "7" -> SEVEN;
            case "8" -> EIGHT;
            case "9" -> NINE;
            case "T" -> TEN;
            case "J" -> JACK;
            case "Q" -> QUEEN;
            case "K" -> KING;
            case "A" -> ACE;
            default -> throw new IllegalArgumentException("Illegal value " + symbol);
        };
    }

    public static Value[] valuesInOrder() {
        return values();
    }

    public int value() {
        return ordinal();
    }

    @Override
    public String toString() {
        return presentation;
    }
}
