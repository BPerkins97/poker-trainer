package de.poker.solver.game;


import java.util.Random;

public record Card(Value value, Suit suit, String presentation) implements Comparable<Card> {
    public static final int NUM_CARDS = 52;

    public static final Card[] CARDS = new Card[NUM_CARDS];

    static {
        for (Value v : Value.valuesInOrder()) {
            for (Suit s : Suit.suitsInOrder()) {
                Card c = new Card(v, s, v.toString() + s.toString());
                CARDS[c.toInt()] = c;
            }
        }
    }
    public static Card randomCard(Random random) {
        return Card.of(random.nextInt(NUM_CARDS));
    }

    public static Card of(Value value, Suit suit) {
        return CARDS[value.value() * 4 + suit.value()];
    }

    public static Card of(String card) {
        Value value = Value.parse(String.valueOf(card.charAt(0)));
        Suit suit = Suit.parse(String.valueOf(card.charAt(1)));
        return of(value, suit);
    }

    public int toInt() {
        return value.value() * 4 + suit.value();
    }

    public long toLong() {
        return (long)toInt();
    }

    public static Card of(int card) {
        return CARDS[card];
    }

    @Override
    public String toString() {
        return presentation;
    }

    @Override
    public int compareTo(Card o) {
        return Integer.compare(this.toInt(), o.toInt());
    }

    public byte toByte() {
        return (byte)(value.value() * 4 + suit.value());
    }
}
