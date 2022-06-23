package de.poker.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardDeckTest {

    @Test
    public void givenOneCard_WhenDrawingOneCard_ThenGetTheOnlyCard() {
        CardDeck deck = new CardDeck();
        deck.addCard(Card.of("9d"));

        assertEquals("9d", deck.nextCard().asString());
    }

    @Test
    public void givenTwoCards_WhenDrawingCards_ThenReturnThemInOrder() {
        CardDeck deck = new CardDeck();
        deck.addCard(Card.of("9d"));
        deck.addCard(Card.of("Ts"));

        assertEquals("9d", deck.nextCard().asString());
        assertEquals("Ts", deck.nextCard().asString());
    }
}
