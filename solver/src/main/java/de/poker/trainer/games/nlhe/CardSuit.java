package de.poker.trainer.games.nlhe;

public enum CardSuit {
    SPADES("s"),
    HEARTS("h"),
    DIAMONDS("d"),
    CLUBS("c");

    public final String representation;

    CardSuit(String representation) {
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
