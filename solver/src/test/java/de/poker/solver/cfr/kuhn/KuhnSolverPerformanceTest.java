package de.poker.solver.cfr.kuhn;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ThreadLocalRandom;

public class KuhnSolverPerformanceTest {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(KuhnSolverPerformanceTest.class.getSimpleName())
                //  .addProfiler(StackProfiler.class)
                //.addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }

    //Benchmark                             Mode  Cnt   Score   Error  Units
    //KuhnSolverPerfomanceTest.solverTest  thrpt   25  12,563 ± 1,868  ops/s
    @Benchmark
    @Warmup(time = 1)
    @Measurement(time = 1)
    public double solverTest(Blackhole blackhole) {
        Solver solver = new Solver();
        return solver.train(ThreadLocalRandom.current(), 100000, 0.01, false);
    }
}
