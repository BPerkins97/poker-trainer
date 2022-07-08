package de.poker.solver.utility;

import de.poker.solver.Main;
import de.poker.solver.game.Card;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PermutationGeneratorPerformanceTest {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PermutationGeneratorPerformanceTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    //Benchmark                             Mode  Cnt   Score   Error  Units
    //PermutationGeneratorPerformanceTest.solverTest  thrpt   25  4181,363 ± 160,225  ops/s
    //PermutationGeneratorPerformanceTest.solverTest  thrpt   25  11545,530 ± 67,699  ops/s
    @Benchmark
    @Warmup(time = 1)
    @Measurement(time = 1)
    public void solverTest(Blackhole blackhole) {
        List<Card> cards = Main.generateDeck(6, ThreadLocalRandom.current());
        List<TreeList> permutations = TreeList.generate(cards);
        blackhole.consume(permutations);
    }
}
