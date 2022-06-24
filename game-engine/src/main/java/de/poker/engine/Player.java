package de.poker.engine;

public class Player {
    private final double startingStack;
    private final HoleCards holeCards;

    public Player(double startingStack, HoleCards holeCards) {
        this.startingStack = startingStack;
        this.holeCards = holeCards;
    }

    public static Player of(double startingStack, String card1, String card2) {
        return new Player(startingStack, HoleCards.of(card1, card2));
    }

    public HoleCards holeCards() {
        return holeCards;
    }
}
