package de.poker.engine;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.poker.engine.utility.Assert.assertThat;

public class Card implements Comparable<Card> {
    private final Value value;
    private final Suit suit;
    private final int card;

    private Card(Value value, Suit suit) {
        this.value = value;
        this.suit = suit;
        this.card = value.value * 4 + suit.value;
    }

    public Value value() {
        return value;
    }

    public Suit suit() {
        return suit;
    }

    public static Card of(String card) {
        assertThat(card != null, "You can not instantiate a card from a null value");
        assertThat(card.length() == 2, "The value you provided can not be a valid card: " + card);
        assertThat(isLegalCard(card), card + " is not a valid card"); // TODO redundant check?
        Value value = Value.parse(card.charAt(0));
        Suit suit = Suit.parse(card.charAt(1));
        return new Card(value, suit);
    }

    private static boolean isLegalCard(String card) {
        Pattern compile = Pattern.compile("[23456789TJQKA][dsch]");
        Matcher matcher = compile.matcher(card);
        return matcher.matches();
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(value)
                .append(suit)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card1 = (Card) o;
        return value == card1.value && suit == card1.suit;
    }

    @Override
    public int hashCode() {
        return card;
    }

    @Override
    public int compareTo(Card card1) {
        return Integer.compare(this.card, card1.card);
    }

    public enum Value {
        TWO(0, '2'),
        THREE(1, '3'),
        FOUR(2, '4'),
        FIVE(3, '5'),
        SIX(4, '6'),
        SEVEN(5, '7'),
        EIGHT(6, '8'),
        NINE(7, '9'),
        TEN(8, 'T'),
        JACK(9, 'J'),
        QUEEN(10, 'Q'),
        KING(11, 'K'),
        ACE(12, 'A');

        private final int value;
        private final char symbol;

        Value(int value, char symbol) {
            this.value = value;
            this.symbol = symbol;
        }

        public static Value parse(char symbol) {
            for (Value value : Value.values()) {
                if (value.symbol == symbol) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Illegal value " + symbol);
        }

        @Override
        public String toString() {
            return String.valueOf(symbol);
        }
    }

    public enum Suit {
        SPADES(3, 's'),
        CLUB(0, 'c'),
        HEART(2, 'h'),
        DIAMOND(1, 'd')
        ;

        private final int value;
        private final char symbol;

        Suit(int value, char symbol) {
            this.value = value;
            this.symbol = symbol;
        }

        public static Suit parse(char symbol) {
            for (Suit suit : Suit.values()) {
                if (suit.symbol == symbol) {
                    return suit;
                }
            }
            throw new IllegalArgumentException("Illegal suit " + symbol);
        }

        @Override
        public String toString() {
            return String.valueOf(symbol);
        }
    }
}
