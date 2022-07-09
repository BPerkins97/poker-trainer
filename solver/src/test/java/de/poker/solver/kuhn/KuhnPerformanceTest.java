package de.poker.solver.kuhn;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class KuhnPerformanceTest {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(KuhnPerformanceTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    //Benchmark                        Mode  Cnt   Score   Error  Units
    //KuhnPerformanceTest.solverTest  thrpt   25  34,867 ± 2,252  ops/s
    //KuhnPerformanceTest.solverTest  thrpt   25  39,467 ± 1,177  ops/s
    @Benchmark
    @Warmup(time = 1)
    @Measurement(time = 1)
    public void solverTest(Blackhole blackhole) {
        Solver solver = new Solver();
        double train = solver.train(1_000_000, 0.01, false);
        blackhole.consume(train);
    }

}
