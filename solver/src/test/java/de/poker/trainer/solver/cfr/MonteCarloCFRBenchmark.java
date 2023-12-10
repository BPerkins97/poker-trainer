package de.poker.trainer.solver.cfr;

import de.poker.trainer.games.kuhn.KuhnGameFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

public class MonteCarloCFRBenchmark {
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    /**
     * ~ 90 ops / s
     * @param state
     */
    @Benchmark
    public void init(MyState state) {
        MonteCarloCFR<String, String> cfr = new MonteCarloCFR<>(state.nodeMap, new KuhnGameFactory(), 2);
        cfr.run(10_000);
    }

    @State(Scope.Benchmark)
    public static class MyState {
        InMemoryNodeMap<String, String> nodeMap = new InMemoryNodeMap<>();
    }
}
