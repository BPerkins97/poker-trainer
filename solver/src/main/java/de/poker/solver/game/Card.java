package de.poker.solver.game;


import java.util.Random;

import static de.poker.solver.game.Suit.*;
import static de.poker.solver.game.Value.*;

// TODO I dont know if i like this yet, maybe revert later, doesnt seem to bring performance improvements
public enum Card implements Comparable<Card> {
    TWO_SPADE(TWO, SPADES),
    TWO_HEARTS(TWO, HEART),
    TWO_DIAMONDS(TWO, DIAMOND),
    TWO_CLUB(TWO, CLUB),

    THREE_SPADE(THREE, SPADES),
    THREE_HEART(THREE, HEART),
    THREE_DIAMONDS(THREE, DIAMOND),
    THREE_CLUB(THREE, CLUB),

    FOUR_SPADE(FOUR, SPADES),
    FOUR_HEART(FOUR, HEART),
    FOUR_DIAMONDS(FOUR, DIAMOND),
    FOUR_CLUB(FOUR, CLUB),

    FIVE_SPADE(FIVE, SPADES),
    FIVE_HEART(FIVE, HEART),
    FIVE_DIAMONDS(FIVE, DIAMOND),
    FIVE_CLUB(FIVE, CLUB),

    SIX_SPADE(SIX, SPADES),
    SIX_HEART(SIX, HEART),
    SIX_DIAMONDS(SIX, DIAMOND),
    SIX_CLUB(SIX, CLUB),

    SEVEN_SPADE(SEVEN, SPADES),
    SEVEN_HEART(SEVEN, HEART),
    SEVEN_DIAMONDS(SEVEN, DIAMOND),
    SEVEN_CLUB(SEVEN, CLUB),

    EIGHT_SPADE(EIGHT, SPADES),
    EIGHT_HEART(EIGHT, HEART),
    EIGHT_DIAMONDS(EIGHT, DIAMOND),
    EIGHT_CLUB(EIGHT, CLUB),

    NINE_SPADE(NINE, SPADES),
    NINE_HEART(NINE, HEART),
    NINE_DIAMONDS(NINE, DIAMOND),
    NINE_CLUB(NINE, CLUB),

    TEN_SPADE(TEN, SPADES),
    TEN_HEART(TEN, HEART),
    TEN_DIAMONDS(TEN, DIAMOND),
    TEN_CLUB(TEN, CLUB),

    JACK_SPADE(JACK, SPADES),
    JACK_HEART(JACK, HEART),
    JACK_DIAMONDS(JACK, DIAMOND),
    JACK_CLUB(JACK, CLUB),

    QUEEN_SPADE(QUEEN, SPADES),
    QUEEN_HEART(QUEEN, HEART),
    QUEEN_DIAMONDS(QUEEN, DIAMOND),
    QUEEN_CLUB(QUEEN, CLUB),

    KING_SPADE(KING, SPADES),
    KING_HEART(KING, HEART),
    KING_DIAMONDS(KING, DIAMOND),
    KING_CLUB(KING, CLUB),

    ACE_SPADE(ACE, SPADES),
    ACE_HEART(ACE, HEART),
    ACE_DIAMONDS(ACE, DIAMOND),
    ACE_CLUB(ACE, CLUB),
    ;

    public static final int NUM_CARDS = 52;

    private static final Card[] CARDS = new Card[52];

    static {
        for (Card card : Card.values()) {
            CARDS[card.ordinal()] = card;
        }
    }

    private final Value value;
    private final Suit suit;

    Card(Value value, Suit suit) {
        this.value = value;
        this.suit = suit;
    }

    public static Card randomCard(Random random) {
        return Card.of(random.nextInt(NUM_CARDS));
    }

    public Value value() {
        return value;
    }

    public Suit suit() {
        return suit;
    }

    public static Card of(Value value, Suit suit) {
        return CARDS[value.value() * 4 + suit.value()];
    }

    public static Card of(String card) {
        Value value = Value.parse(card.charAt(0));
        Suit suit = Suit.parse(card.charAt(1));
        return CARDS[value.value() * 4 + suit.value()];
    }

    public int toInt() {
        return ordinal();
    }

    public static Card of(int card) {
        return CARDS[card];
    }

    @Override
    public String toString() {
        return String.valueOf(value) +
                suit;
    }
}
