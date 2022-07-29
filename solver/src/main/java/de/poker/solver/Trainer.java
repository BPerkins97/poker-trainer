package de.poker.solver;

import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.map.persistence.FileSystem;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static de.poker.solver.MonteCarloCFR.*;

public class Trainer {
    private final ThreadPoolExecutor executorService;
    volatile int iterations;
    long startTime;

    volatile boolean running = false;

    public Trainer() {
        executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(ApplicationConfiguration.NUM_THREADS);
    }

    public void start() {
        run();
    }

    public void stop() {
        running = false;
    }

    private void run() {
        startTime = System.currentTimeMillis();
        do {
            executorService.execute(this::doIteration);
            preventQueueFromOvergrowing();
        } while (running);
        try {
            executorService.awaitTermination(5L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            FileSystem.close();
        }
    }

    public void printDebugInfo() {
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
        }
        iterations++;
        // TODO enable this later seems to be broken updateStrategy(rootNode, ThreadLocalRandom.current());
    }
}
