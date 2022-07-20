package de.poker.solver;

import de.poker.solver.game.Card;

import java.util.concurrent.ThreadLocalRandom;

public class HoldEmConfiguration {
    public int strategyInterval() {
        return 1;
    }

    public int pruningThreshold() {
        return 200;
    }

    public int linearCFRThreshold() {
        return 400;
    }

    public int discountInterval() {
        return 100;
    }

    // -310_000_000 for Pluribus
    public int minimumRegret() {
        return -310_000_000;
    }

    public int numPlayers() {
        return HoldEmConstants.NUM_PLAYERS;
    }

    public HoldEmGameTree randomRootNode() {
        int numCards = numPlayers() * 2 + 5;
        Card[] deck = new Card[numCards];
        for (int i = 0; i < numCards; i++) {
            Card card;
            do {
                card = Card.randomCard(ThreadLocalRandom.current());
            } while (cardAlreadyInDeck(deck, card, i));
            deck[i] = card;
        }
        return new HoldEmGameTree(deck);
    }

    private boolean cardAlreadyInDeck(Card[] deck, Card card, int insertAtPosition) {
        for (int i = 0; i < insertAtPosition; i++) {
            if (deck[i] == card) {
                return true;
            }
        }
        return false;
    }
}
