package de.poker.trainer.games.nlhe;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HandRankerPerformanceTest {
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    /**
     * ~ 125k ops/s
     */
    @Benchmark
    public void rank(MyState state, Blackhole bh) {
        List<Card> cards = new ArrayList<>();
        while (cards.size() < 7) {
            Card card = getRandomCard(state.random);
            while (cards.contains(card)) {
                card = getRandomCard(state.random);
            }
            cards.add(card);
        }
        bh.consume(HandRanker.rank(cards));
    }

    private Card getRandomCard(Random random) {
        int suit = random.nextInt(CardSuit.values().length);
        int rank = random.nextInt(CardRank.values().length);
        return new Card(CardRank.values()[rank], CardSuit.values()[suit]);
    }

    @State(Scope.Benchmark)
    public static class MyState {
        final Random random = new Random();
    }
}
