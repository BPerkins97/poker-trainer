package de.poker.solver.pluribus.kuhn;

import de.poker.solver.pluribus.Configuration;
import de.poker.solver.pluribus.GameTree;

import java.util.concurrent.ThreadLocalRandom;

public class KuhnConfiguration implements Configuration {
    @Override
    public int strategyInterval() {
        return 10000;
    }

    @Override
    public int pruningThreshold() {
        return 200;
    }

    @Override
    public int linearCFRThreshold() {
        return 800;
    }

    @Override
    public int discountInterval() {
        return 10;
    }

    @Override
    public int minimumRegret() {
        return -310000;
    }

    @Override
    public int numPlayers() {
        return 2;
    }

    @Override
    public GameTree randomRootNode() {
        int card1 = ThreadLocalRandom.current().nextInt(0, 3);
        int card2;
        do {
            card2 = ThreadLocalRandom.current().nextInt(0, 3);
        } while (card1 == card2);
        int[] cards = new int[2];
        cards[0] = card1;
        cards[1] = card1;
        return new KuhnGameTree(cards);
    }
}
