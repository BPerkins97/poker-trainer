package kuhn.cfr;

import algorithms.vanillacfr.Game;
import algorithms.vanillacfr.GameFactory;

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
