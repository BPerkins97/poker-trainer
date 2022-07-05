package de.poker.solver.cfr;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ThreadLocalRandom;

public class PerformanceTest {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PerformanceTest.class.getSimpleName())
              //  .addProfiler(StackProfiler.class)
                    .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }

    //Benchmark                    Mode  Cnt  Score   Error  Units
    //PerformanceTest.solverTest  thrpt   25  1,150 ± 0,085  ops/s
    @Benchmark
    @Warmup(time = 1)
    @Measurement(time = 1)
    public void solverTest(Blackhole blackhole) {
        de.poker.solver.cfr.holdem.Solver solver = new de.poker.solver.cfr.holdem.Solver();
        double[] train = solver.train(1, ThreadLocalRandom.current());
        blackhole.consume(train);
    }
}
