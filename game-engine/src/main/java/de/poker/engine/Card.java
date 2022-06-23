package de.poker.engine;

public class Card {
    // TODO this could be an integer or something for performance improvements, better yet, let it be Enums
    private final String card;

    private Card(String value) {
        this.card = value;
    }

    public static Card of(String value) {
        if ("99".equals(value)) {
            throw new IllegalArgumentException("99 is not a valid card.");
        }
        return new Card(value);
    }

    public String asString() {
        return card;
    }
}
