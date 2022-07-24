package de.poker.solver;

import de.poker.solver.database.Database;
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

    private void preventQueueFromOvergrowing() {;
        while (executorService.getQueue().size() > ApplicationConfiguration.NUM_THREADS * 5) {
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
            nodeMap = Database.getNodes(rootNode);
        } catch (SQLException e) {
            e.printStackTrace();
            // If we fail during the select we dont care, we simply inform about the error  and return
            return;
        }
        for  (int i=0;i<100;i++) {
            for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
                traverseMCCFR_WithPruning(nodeMap, rootNode, p, ThreadLocalRandom.current());
            }
        }
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            testForStrategy(rootNode, p, nodeMap);
        }
        incrementIterations();

        try {
            Database.updateNodes(nodeMap);
        } catch (SQLException e) {
            // TODO think about what we could do here
            throw new RuntimeException(e);
        }
    }

    private void testForStrategy(HoldEmGameTree rootNode, int player, NodeMap nodeMap) {
        if (iterations >= ApplicationConfiguration.STRATEGY_THRESHOLD && iterations % ApplicationConfiguration.STRATEGY_INTERVAL == 0) {
            updateStrategy(nodeMap, rootNode, player, ThreadLocalRandom.current());
        }
    }

    private synchronized void incrementIterations() {
        iterations++;
        if (iterations % 100 == 0) {
            printDebugInfo();
        }
        if (iterations > 100) {
            System.exit(0);
        }
    }

    private void doIterationNoPruning() {
        HoldEmGameTree rootNode = HoldEmGameTree.getRandomRootState();
        NodeMap nodeMap;
        try {
            nodeMap = Database.getNodes(rootNode);
        } catch (SQLException e) {
            e.printStackTrace();
            // If we fail during the select we dont care, we simply inform about the error  and return
            return;
        }
        for  (int i=0;i<100;i++) {
            for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
                traverseMCCFR_NoPruning(nodeMap, rootNode, p, ThreadLocalRandom.current());
            }
        }
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            testForStrategy(rootNode, p, nodeMap);
        }
        incrementIterations();
        try {
            Database.updateNodes(nodeMap);
        } catch (SQLException e) {
            // TODO think about what we could do here
            throw new RuntimeException(e);
        }
    }
}
