package de.poker.solver;

import de.poker.solver.database.DAO;
import de.poker.solver.database.NodeMap;
import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;

import static de.poker.solver.MonteCarloCFR.*;

public class Trainer {
    private final ThreadPoolExecutor executorService;
    int iterations;
    long startTime;
    private final DAO dao;

    public Trainer() throws SQLException {
        dao = new DAO();
        executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);
    }

    public void start() {
        run();
    }

    private long calculateNextDiscountTimestamp() {
        return System.currentTimeMillis() + ApplicationConfiguration.DISCOUNT_INTERVAL * 60 * 1000;
    }

    private void run() {
        startTime = System.currentTimeMillis();
        printDebugInfo();
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
        } while (true);
    }

    private void printDebugInfo() {
        long time = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Ran for " + time + " seconds and iterated " + iterations + " times");
    }

    private void testForDiscount() {
        // TODO this can be implemented in the database as trigger
//        if (iterations % ApplicationConfiguration.DISCOUNT_INTERVAL == 0 && iterations < ApplicationConfiguration.DISCOUNT_THRESHOLD) {
//            System.out.println("Now performing discount");
//            double discountValue = calculateDiscountValue(iterations % ApplicationConfiguration.DISCOUNT_INTERVAL);
//            dao.discount(discountValue);
//        }
    }

    private void preventQueueFromOvergrowing() {;
        while (executorService.getQueue().size() > executorService.getMaximumPoolSize() * 10) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doIterationWithPruning() {
        HoldEmGameTree rootNode = HoldEmGameTree.getRandomRootState();
        NodeMap nodeMap;
        try {
            nodeMap = dao.getNodes(rootNode);
        } catch (SQLException e) {
            e.printStackTrace();
            // If we fail during the select we dont care, we simply inform about the error  and return
            return;
        }
        for  (int i=0;i<100;i++) {
            for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
                traverseMCCFR_WithPruning(nodeMap, rootNode, p);
            }
        }
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            testForStrategy(rootNode, p, nodeMap);
        }
        incrementIterations();

        try {
            dao.updateNodes(nodeMap);
        } catch (SQLException e) {
            // TODO think about what we could do here
            throw new RuntimeException(e);
        }
    }

    private void testForStrategy(HoldEmGameTree rootNode, int player, NodeMap nodeMap) {
        if (iterations >= ApplicationConfiguration.STRATEGY_THRESHOLD && iterations % ApplicationConfiguration.STRATEGY_INTERVAL == 0) {
            updateStrategy(nodeMap, rootNode, player);
        }
    }

    private synchronized void incrementIterations() {
        iterations++;
        testForDiscount();
        if (iterations % 1000 == 0) {
            printDebugInfo();
        }
    }

    private void doIterationNoPruning() {
        HoldEmGameTree rootNode = HoldEmGameTree.getRandomRootState();
        NodeMap nodeMap;
        try {
            nodeMap = dao.getNodes(rootNode);
        } catch (SQLException e) {
            e.printStackTrace();
            // If we fail during the select we dont care, we simply inform about the error  and return
            return;
        }
        for  (int i=0;i<100;i++) {
            for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
                traverseMCCFR_NoPruning(nodeMap, rootNode, p);
            }
        }
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            testForStrategy(rootNode, p, nodeMap);
        }
        incrementIterations();
        try {
            dao.updateNodes(nodeMap);
        } catch (SQLException e) {
            // TODO think about what we could do here
            throw new RuntimeException(e);
        }
    }

    private static double calculateDiscountValue(int numDiscount) {
        return numDiscount / (numDiscount + 1);
    }
}
