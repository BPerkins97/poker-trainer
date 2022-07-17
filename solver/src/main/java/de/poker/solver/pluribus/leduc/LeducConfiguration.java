package de.poker.solver.pluribus.leduc;

import de.poker.solver.pluribus.Configuration;
import de.poker.solver.pluribus.GameTree;

import java.util.concurrent.ThreadLocalRandom;

public class LeducConfiguration implements Configuration {
    @Override
    public int strategyInterval() {
        return 1;
    }

    @Override
    public int pruningThreshold() {
        return 10000;
    }

    @Override
    public int linearCFRThreshold() {
        return 50000;
    }

    @Override
    public int discountInterval() {
        return 5000;
    }

    @Override
    public int minimumRegret() {
        return -300_000;
    }

    @Override
    public int numPlayers() {
        return 2;
    }

    @Override
    public GameTree randomRootNode() {
        int[] cards = new int[3];

        cards[0] = ThreadLocalRandom.current().nextInt(0,LeducConstants.NUM_CARDS);
        cards[1] = ThreadLocalRandom.current().nextInt(0,LeducConstants.NUM_CARDS);

        for (int i=2;i<3;i++) {
            do {
                cards[i] = ThreadLocalRandom.current().nextInt(0,LeducConstants.NUM_CARDS);
            } while (!cardNotAlreadyTwiceInDeck(cards, i));
        }

        return new LeducGameTree(cards);
    }

    private boolean cardNotAlreadyTwiceInDeck(int[] cards, int index) {
        int cardCounter = 0;
        for (int i=0;i<index;i++) {
            if (cards[i] == cards[index]) {
                cardCounter++;
            }
        }
        return cardCounter <= 2;
    }
}
