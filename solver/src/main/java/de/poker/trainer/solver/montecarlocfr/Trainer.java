package de.poker.trainer.solver.montecarlocfr;

import de.poker.trainer.solver.montecarlocfr.game.Constants;
import de.poker.trainer.solver.montecarlocfr.game.HoldEmGameTree;
import de.poker.trainer.solver.montecarlocfr.map.persistence.FileSystem;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Trainer {
    private final ThreadPoolExecutor executorService;
    volatile int iterations;
    long startTime;

    volatile boolean running = false;

    public Trainer() {
        executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(ApplicationConfiguration.NUM_THREADS);
    }

    public void start() {
        running = true;
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
        executorService.getQueue().clear();
        try {
            executorService.awaitTermination(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();
        System.out.println("stopping");
        FileSystem.close();
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
            MonteCarloCFR.traverse(rootNode, p, ThreadLocalRandom.current());
        }
        incrementIteration();
        // TODO enable this later seems to be broken updateStrategy(rootNode, ThreadLocalRandom.current());
    }

    private synchronized void incrementIteration() {
        iterations++;
    }
}
