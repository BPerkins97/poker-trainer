package de.poker.solver;

import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;

import static de.poker.solver.MonteCarloCFR.*;

public class Trainer {
    private final ThreadPoolExecutor executorService;
    int iterations;
    long startTime;

    public Trainer() {
        executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(ApplicationConfiguration.NUM_THREADS);
    }

    public void start() {
        run();
    }

    private void run() {
        startTime = System.currentTimeMillis();
        printDebugInfo();
        do {
            executorService.execute(this::doIteration);
            preventQueueFromOvergrowing();
        } while (true);
    }

    private void printDebugInfo() {
        long time = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Ran for " + time + " seconds and iterated " + iterations + " times");
    }

    private void preventQueueFromOvergrowing() {
        while (executorService.getQueue().size() > ApplicationConfiguration.NUM_THREADS * 5) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doIteration() {
        HoldEmGameTree rootNode = HoldEmGameTree.getRandomRootState();
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            traverse(rootNode, p, ThreadLocalRandom.current());
            testForStrategy(rootNode, p);
        }
        incrementIterations();
    }

    private void testForStrategy(HoldEmGameTree rootNode, int player) {
        // TODO this has a memory leakupdateStrategy(nodeMap, rootNode, player, ThreadLocalRandom.current());
    }

    private synchronized void incrementIterations() {
        iterations++;
        if (iterations % 100 == 0) {
            printDebugInfo();
        }
    }
}
