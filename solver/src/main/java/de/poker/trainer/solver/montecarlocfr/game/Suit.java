package de.poker.trainer.solver.montecarlocfr.game;

public enum Suit {
    SPADES("s"),
    HEART("h"),
    DIAMOND("d"),
    CLUB("c");

    private static final Suit[] SUITS = new Suit[4];

    static {
        for (Suit suit : Suit.values()) {
            SUITS[suit.ordinal()] = suit;
        }
    }

    final String presentation;

    Suit(String presentation) {
        this.presentation = presentation;
    }

    public static Suit[] suitsInOrder() {
        return values();
    }

    public static Suit of(byte suit) {
        return SUITS[suit];
    }

    public int value() {
        return ordinal();
    }

    public static Suit parse(String symbol) {
        return switch (symbol) {
            case "s" -> SPADES;
            case "h" -> HEART;
            case "c" -> CLUB;
            case "d" -> DIAMOND;
            default -> throw new IllegalArgumentException("Illegal symbol " + symbol);
        };
    }

    @Override
    public String toString() {
        return presentation;
    }
}
