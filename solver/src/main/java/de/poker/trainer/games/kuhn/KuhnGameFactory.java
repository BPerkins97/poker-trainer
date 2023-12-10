package de.poker.trainer.games.kuhn;

import de.poker.trainer.solver.cfr.Game;
import de.poker.trainer.solver.cfr.GameFactory;

import java.util.concurrent.ThreadLocalRandom;

public class KuhnGameFactory implements GameFactory<String, String> {
    @Override
    public Game<String, String> generate() {
        int[] deck = new int[2];
        deck[0] = ThreadLocalRandom.current().nextInt(3);
        do {
            deck[1] = ThreadLocalRandom.current().nextInt(3);
        } while (deck[0] == deck[1]);
        return new KuhnGame(deck);
    }
}
