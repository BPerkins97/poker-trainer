package de.poker.engine;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.poker.engine.utility.Assert.assertThat;

public class Card implements Comparable<Card> {
    private final short value;
    private final byte suit;
    private final int card;

    private Card(short value, byte suit) {
        this.value = value;
        this.suit = suit;
        this.card = value * 4 + suit;
    }

    public static Card of(String card) {
        assertThat(card != null, "You can not instantiate a card from a null value");
        assertThat(card.length() == 2, "The value you provided can not be a valid card: " + card);
        assertThat(isLegalCard(card), card + " is not a valid card"); // TODO redundant check?
        short value = parseCardValue(card.charAt(0));
        byte suit = parseCardSuit(card.charAt(1));
        return new Card(value, suit);
    }

    private static byte parseCardSuit(char suit) {
        switch (suit) {
            case 'd': return 0;
            case 's': return 1;
            case 'c': return 2;
            case 'h': return 3;
            default: throw new IllegalArgumentException("Illegal card suit " + suit);
        }
    }

    private static short parseCardValue(char value) {
        switch (value) {
            case '2': return 0;
            case '3': return 1;
            case '4': return 2;
            case '5': return 3;
            case '6': return 4;
            case '7': return 5;
            case '8': return 6;
            case '9': return 7;
            case 'T': return 8;
            case 'J': return 9;
            case 'Q': return 10;
            case 'K': return 11;
            case 'A': return 12;
            default: {
                throw new IllegalArgumentException("Illegal card value " + value);
            }
        }
    }

    private static boolean isLegalCard(String card) {
        Pattern compile = Pattern.compile("[23456789TJQKA][dsch]");
        Matcher matcher = compile.matcher(card);
        return matcher.matches();
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
        return value * 4 + suit;
    }

    @Override
    public int compareTo(Card card1) {
        return Integer.compare(this.card, card1.card);
    }
}
