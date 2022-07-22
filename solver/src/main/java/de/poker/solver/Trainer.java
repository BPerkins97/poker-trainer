package de.poker.solver;

import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.map.HoldEmNodeMap;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;

import static de.poker.solver.MonteCarloCFR.*;

public class Trainer {
    private volatile boolean isRunning = false;
    private HoldEmNodeMap nodeMap = new HoldEmNodeMap();
    private ThreadPoolExecutor executorService;
    public File file;
    int iterations;

    public Trainer() {
        executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void start() {
        isRunning = true;
        run();
    }

    private long calculateNextDiscountTimestamp() {
        return System.currentTimeMillis() + ApplicationConfiguration.DISCOUNT_INTERVAL * 60 * 1000;
    }

    public void stop() {
        isRunning = false;
    }

    private void run() {
        do {
            double randomNumber = ThreadLocalRandom.current().nextDouble();
            boolean prune = iterations > ApplicationConfiguration.PRUNING_THRESHOLD && randomNumber > 0.05;
            if (prune) {
                executorService.execute(this::doIterationWithPruning);
            } else {
                executorService.execute(this::doIterationNoPruning);
            }

            // TODO
//                if (i % ApplicationConfiguration.STRATEGY_INTERVAL == 0) {
//                    updateStrategy(nodeMap, rootNode, p);
//                }
            preventQueueFromOvergrowing();
        } while (isRunning);
        try {
            save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testForDiscount() {
        if (iterations % ApplicationConfiguration.DISCOUNT_INTERVAL == 0 && iterations < ApplicationConfiguration.DISCOUNT_THRESHOLD) {
            System.out.println("Now performing discount");
            double discountValue = calculateDiscountValue(iterations % ApplicationConfiguration.DISCOUNT_INTERVAL);
            nodeMap.discount(discountValue);
        }
    }

    private void preventQueueFromOvergrowing() {;
        while (executorService.getQueue().size() > executorService.getMaximumPoolSize() * 10) {
            Thread.yield();
        }
    }

    private void doIterationWithPruning() {
        HoldEmGameTree rootNode = HoldEmGameTree.getRandomRootState();
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            traverseMCCFR_WithPruning(nodeMap, rootNode, p);
            testForStrategy(rootNode, p);
        }
        incrementIterations();
    }

    private void testForStrategy(HoldEmGameTree rootNode, int player) {
        if (iterations % ApplicationConfiguration.STRATEGY_INTERVAL == 0) {
            updateStrategy(nodeMap, rootNode, player);
        }
    }

    private synchronized void incrementIterations() {
        iterations++;
        testForDiscount();
    }

    private void doIterationNoPruning() {
        HoldEmGameTree rootNode = HoldEmGameTree.getRandomRootState();
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            traverseMCCFR_NoPruning(nodeMap, rootNode, p);
            testForStrategy(rootNode, p);
        }
        incrementIterations();
    }

    private static double calculateDiscountValue(int numDiscount) {
        return numDiscount / (numDiscount + 1);
    }

    public void loadFile(File file) throws IOException {
        this.file = file;
        nodeMap.loadFromFile(file);
    }

    public void save(File file) throws IOException {
        nodeMap.saveToFile(file);
    }
}
