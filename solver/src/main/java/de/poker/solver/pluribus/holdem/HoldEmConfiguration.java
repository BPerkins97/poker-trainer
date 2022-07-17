package de.poker.solver.pluribus.holdem;

import de.poker.solver.game.Card;
import de.poker.solver.pluribus.Configuration;
import de.poker.solver.pluribus.GameTree;

import java.util.concurrent.ThreadLocalRandom;

public class HoldEmConfiguration implements Configuration {
    @Override
    public int strategyInterval() {
        return 100;
    }

    @Override
    public int pruningThreshold() {
        return 200;
    }

    @Override
    public int linearCFRThreshold() {
        return 400;
    }

    @Override
    public int discountInterval() {
        return 100;
    }

    // -310_000_000 for Pluribus
    @Override
    public int minimumRegret() {
        return -310_000_000;
    }

    @Override
    public int numPlayers() {
        return HoldEmConstants.NUM_PLAYERS;
    }

    @Override
    public GameTree randomRootNode() {
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
