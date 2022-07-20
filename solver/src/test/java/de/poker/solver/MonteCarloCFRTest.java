package de.poker.solver;

import de.poker.solver.map.HoldEmNodeMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class MonteCarloCFRTest {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MonteCarloCFRTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    //Benchmark                Mode  Cnt  Score   Error  Units
    //MonteCarloCFRTest.test  thrpt   25  0,302 ± 0,053  ops/s
    @Benchmark
    @Warmup(time = 1, iterations = 3)
    @Measurement(time = 1)
    public void test(MyState myState) {
        MonteCarloCFR.mccfr_Pruning(1000, myState.nodeMap);
    }

    @State(Scope.Benchmark)
    public static class MyState {
        public HoldEmNodeMap nodeMap = new HoldEmNodeMap();
    }
}
