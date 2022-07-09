package de.poker.solver.cfr;

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
                .include(SolverPerformanceTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    //Benchmark                          Mode  Cnt      Score      Error  Units
    //SolverPerformanceTest.solverTest  thrpt   25  36215,051 ± 1402,028  ops/s
    //SolverPerformanceTest.solverTest  thrpt   25  43479,463 ± 1793,150  ops/s
    @Benchmark
    @Warmup(time = 1)
    @Measurement(time = 1)
    public void solverTest(Blackhole blackhole) {
        Solver solver = new Solver();
        double[] train = solver.train(1, ThreadLocalRandom.current());
        blackhole.consume(train);
    }
}
