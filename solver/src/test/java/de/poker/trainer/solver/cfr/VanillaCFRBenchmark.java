package de.poker.trainer.solver.cfr;

import de.poker.trainer.games.kuhn.KuhnGameFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

public class VanillaCFRBenchmark {
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    /**
     * ~ 105 ops / s
     */
    @Benchmark
    public void init(MyState state) {
        VanillaCFR<String, String> cfr = new VanillaCFR<>(state.nodeMap, new KuhnGameFactory(), 2);
        cfr.setPruningThreshhold(-100);
        cfr.run(10_000);
    }

    @State(Scope.Benchmark)
    public static class MyState {
        final InMemoryNodeMap<String, String> nodeMap = new InMemoryNodeMap<>();
    }
}
