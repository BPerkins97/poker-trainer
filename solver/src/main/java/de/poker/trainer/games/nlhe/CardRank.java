package de.poker.trainer.games.nlhe;

public enum CardRank {
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("T"),
    JACK("J"),
    QUEEN("Q"),
    KING("K"),
    ACE("A");

    public final String representation;

    CardRank(String representation) {
        this.representation = representation;
    }

    public int value() {
        return ordinal();
    }

    @Override
    public String toString() {
        return representation;
    }
}
