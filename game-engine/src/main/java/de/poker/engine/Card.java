package de.poker.engine;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.poker.engine.utility.Assert.assertThat;

public class Card {
    // TODO this could be an integer or something for performance improvements, better yet, let it be Enums
    private final String card;

    private Card(String value) {
        this.card = value;
    }

    public static Card of(String value) {
        assertThat(value != null, "You can not instantiate a card from a null value");
        assertThat(value.length() == 2, "The value you provided can not be a valid card: " + value);
        assertThat(isLegalCard(value), value + " is not a valid card");
        return new Card(value);
    }

    private static boolean isLegalCard(String card) {
        Pattern compile = Pattern.compile("[23456789TJQKA][dsch]");
        Matcher matcher = compile.matcher(card);
        return matcher.matches();
    }

    public String asString() {
        return card;
    }
}
