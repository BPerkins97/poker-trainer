package de.poker.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardDeckTest {

    @Test
    public void givenOneCard_WhenDrawingOneCard_ThenGetTheOnlyCard() {
        CardDeck deck = new CardDeck();
        deck.addCard(Card.of("9d"));

        assertEquals(Card.of("9d"), deck.drawCard());
    }

    @Test
    public void givenTwoCards_WhenDrawingCards_ThenReturnThemInOrder() {
        CardDeck deck = new CardDeck();
        deck.addCard(Card.of("9d"));
        deck.addCard(Card.of("Ts"));

        assertEquals(Card.of("9d"), deck.drawCard());
        assertEquals(Card.of("Ts"), deck.drawCard());
    }

    @Test
    public void givenNoCards_WhenDrawingCards_ThenThrowException() {
        CardDeck deck = new CardDeck();

        try {
            deck.drawCard();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
}
