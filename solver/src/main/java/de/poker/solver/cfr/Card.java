package de.poker.solver.cfr;


public class Card implements Comparable<Card> {
    private final Value value;
    private final Suit suit;
    private final int card;

    public Card(Value value, Suit suit) {
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

    public String forInfoSet() {
        return value.toString() + suit.toString();
    }

    public static Card of(String card) {
        assert card != null : "You can not instantiate a card from a null value";
        assert card.length() == 2 : "The value you provided can not be a valid card: " + card;

        Value value = Value.parse(card.charAt(0));
        Suit suit = Suit.parse(card.charAt(1));
        return new Card(value, suit);
    }

    public static Card of(int card) {
        Value value = Value.of(card / 4);
        Suit suit = Suit.of(card % 4);
        return new Card(value, suit);
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
        TWO(1, '2'),
        THREE(2, '3'),
        FOUR(3, '4'),
        FIVE(4, '5'),
        SIX(5, '6'),
        SEVEN(6, '7'),
        EIGHT(7, '8'),
        NINE(8, '9'),
        TEN(9, 'T'),
        JACK(10, 'J'),
        QUEEN(11, 'Q'),
        KING(12, 'K'),
        ACE(13, 'A');

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
            assert false : "Illegal value " + symbol;
            throw new IllegalArgumentException("Illegal value " + symbol);
        }
        public static Value of(int value) {
            for (Value v : Value.values()) {
                if (v.value == value) {
                    return v;
                }
            }
            assert false : "Illegal value " + value;
            throw new IllegalArgumentException("Illegal value " + value);
        }

        public int value() {
            return value;
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

        public static Suit of(int value) {
            for (Suit suit : Suit.values()) {
                if (suit.value == value) {
                    return suit;
                }
            }
            assert false : "Illegal suit " + value;
            throw new IllegalArgumentException("Illegal suit " + value);
        }

        public static Suit parse(char symbol) {
            for (Suit suit : Suit.values()) {
                if (suit.symbol == symbol) {
                    return suit;
                }
            }
            assert false : "Illegal suit " + symbol;
            throw new IllegalArgumentException("Illegal suit " + symbol);
        }

        @Override
        public String toString() {
            return String.valueOf(symbol);
        }
    }
}
