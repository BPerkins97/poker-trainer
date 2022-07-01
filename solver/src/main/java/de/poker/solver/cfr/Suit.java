package de.poker.solver.cfr;

public enum Suit {
    SPADES('s'),
    HEART('h'),
    DIAMOND('d'),
    CLUB('c');

    private static final Suit[] SUITS = new Suit[4];

    static {
        for (Suit suit : Suit.values()) {
            SUITS[suit.ordinal()] = suit;
        }
    }

    final char symbol;

    Suit(char symbol) {
        this.symbol = symbol;
    }

    public int value() {
        return ordinal();
    }

    public static Suit parse(char symbol) {
        return switch (symbol) {
            case 's' -> SPADES;
            case 'h' -> HEART;
            case 'c' -> CLUB;
            case 'd' -> DIAMOND;
            default -> throw new IllegalArgumentException("Illegal symbol " + symbol);
        };
    }

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }
}
