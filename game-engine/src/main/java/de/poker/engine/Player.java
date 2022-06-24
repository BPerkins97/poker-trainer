package de.poker.engine;

public class Player {
    private double stack;
    private final HoleCards holeCards;
    private final Position position;

    public Player(double startingStack, HoleCards holeCards, Position position) {
        this.stack = startingStack;
        this.holeCards = holeCards;
        this.position = position;
    }

    public static Player smallBlind(double startingStack, String card1, String card2) {
        return of(startingStack, card1, card2, Position.SMALL_BLIND);
    }

    public static Player bigBlind(double startingStack, String card1, String card2) {
        return of(startingStack, card1, card2, Position.BIG_BLIND);
    }

    public static Player loJack(double startingStack, String card1, String card2) {
        return of(startingStack, card1, card2, Position.LO_JACK);
    }

    public static Player hiJack(double startingStack, String card1, String card2) {
        return of(startingStack, card1, card2, Position.HI_JACK);
    }

    public static Player cutOff(double startingStack, String card1, String card2) {
        return of(startingStack, card1, card2, Position.CUT_OFF);
    }

    public static Player button(double startingStack, String card1, String card2) {
        return of(startingStack, card1, card2, Position.BUTTON);
    }

    public static Player of(double startingStack, String card1, String card2, Position position) {
        return new Player(startingStack, HoleCards.of(card1, card2), position);
    }

    public void win(double amount) {
        stack += amount;
    }

    public void pay(double amount) {
        stack -= amount;
    }

    public Position position() {
        return position;
    }

    public double stack() {
        return stack;
    }

    public HoleCards holeCards() {
        return holeCards;
    }

    public static enum Position {
        SMALL_BLIND,
        BIG_BLIND,
        LO_JACK,
        HI_JACK,
        CUT_OFF,
        BUTTON;
    }
}
