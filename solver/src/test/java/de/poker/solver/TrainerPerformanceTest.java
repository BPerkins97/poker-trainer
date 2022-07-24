package de.poker.solver;

import de.poker.solver.database.Database;
import de.poker.solver.database.NodeMap;
import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.sql.SQLException;
import java.util.Random;

import static de.poker.solver.MonteCarloCFR.traverseMCCFR_NoPruning;

@State(Scope.Benchmark)
public class TrainerPerformanceTest {
    private Random random = new Random(123L);

    public static void main(String[] args) throws RunnerException {
        System.out.println(Thread.currentThread().getId());
        Options opt = new OptionsBuilder()
                .include(TrainerPerformanceTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    @Warmup(time = 1, iterations = 0)
    @Measurement(time = 1, iterations = 10)
    public void test(TrainerPerformanceTest state) {
        HoldEmGameTree rootNode = HoldEmGameTree.getRandomRootState(state.random);
        NodeMap nodeMap;
        try {
            nodeMap = Database.getNodes(rootNode);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            traverseMCCFR_NoPruning(nodeMap, rootNode, p, random);
        }
        try {
            Database.updateNodes(nodeMap);
        } catch (SQLException e) {
            // TODO think about what we could do here
            throw new RuntimeException(e);
        }
    }
}
