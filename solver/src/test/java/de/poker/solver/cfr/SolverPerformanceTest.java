package de.poker.solver.cfr;

import de.poker.solver.utility.PermutationGeneratorPerformanceTest;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ThreadLocalRandom;

public class SolverPerformanceTest {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PermutationGeneratorPerformanceTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    //Benchmark                                        Mode  Cnt      Score     Error  Units
    //PermutationGeneratorPerformanceTest.solverTest  thrpt   25  10241,100 ± 868,244  ops/s
    //PermutationGeneratorPerformanceTest.solverTest  thrpt   25  11566,578 ± 81,954  ops/s
    @Benchmark
    @Warmup(time = 1)
    @Measurement(time = 1)
    public void solverTest(Blackhole blackhole) {
        Solver solver = new Solver();
        double[] train = solver.train(1, ThreadLocalRandom.current());
        blackhole.consume(train);
    }
}
