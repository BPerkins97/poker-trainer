package de.poker.solver.cfr;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.StackProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.Map;

public class PerformanceTest {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PerformanceTest.class.getSimpleName())
              //  .addProfiler(StackProfiler.class)
                    //.addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }

    // Benchmark with 1 second warmup and measurement time
    //Benchmark                    Mode  Cnt   Score   Error  Units
    //PerformanceTest.solverTest  thrpt   25  14,825 ± 0,383  ops/s

    //@Benchmark
    @Warmup(time = 1)
    @Measurement(time = 1)
    public void solverTest(Blackhole blackhole) {
        List<List<Card>> cards = Solver.buildDecks(17, 1);
        GameConfiguration gameConfiguration = GameConfiguration.defaultConfig().withCards(cards.get(0));
        GameState gameState = new GameState(gameConfiguration);
        Map<Position, Double> cfr = new Solver().cfr(gameState);
        blackhole.consume(cfr);
    }
//Benchmark                  Mode  Cnt       Score       Error  Units
//PerformanceTest.handTest  thrpt   25  338411,826 ± 32154,831  ops/s
    // PerformanceTest.handTest  thrpt   25  396848,597 ± 4749,007  ops/s -> Map Implementation
    // PerformanceTest.handTest  thrpt   25  426855,927 ± 5050,528  ops/s -> Raw Implementation

    @Benchmark
    @Warmup(time = 1)
    @Measurement(time = 1)
    public Hand handTest() {
        List<List<Card>> cards = Solver.buildDecks(7, 1);
        Hand of = Hand.of(cards.get(0));
        return of;
    }
}
