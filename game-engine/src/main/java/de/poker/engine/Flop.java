package de.poker.engine;

public class Flop {
    private final Card card1;
    private final Card card2;
    private final Card card3;

    public Flop(Card card1, Card card2, Card card3) {
        this.card1 = card1;
        this.card2 = card2;
        this.card3 = card3;
    }

    public static Flop of(String card1, String card2, String card3) {
        return new Flop(Card.of(card1), Card.of(card2), Card.of(card3));
    }
}
