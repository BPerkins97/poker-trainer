package de.poker.solver;

import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.map.HoldEmNodeMap;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import static de.poker.solver.MonteCarloCFR.traverseMCCFR_NoPruning;
import static de.poker.solver.MonteCarloCFR.traverseMCCFR_WithPruning;

public class Trainer {
    private volatile boolean isRunning = false;
    private HoldEmNodeMap nodeMap = new HoldEmNodeMap();
    public int iterations;
    public File file;
    private int numDiscounts;


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
            // TODO boolean prune = i > ApplicationConfiguration.PRUNING_THRESHOLD;
            boolean prune = randomNumber > 0.05;
            if (prune) {
                doIterationWithPruning();
            } else {
                doIterationNoPruning();
            }
            // TODO
//                if (i % ApplicationConfiguration.STRATEGY_INTERVAL == 0) {
//                    updateStrategy(nodeMap, rootNode, p);
//                }
        } while (isRunning);
        try {
            nodeMap.saveToFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void doIterationWithPruning() {
        HoldEmGameTree rootNode = HoldEmGameTree.getRandomRootState();
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            traverseMCCFR_WithPruning(nodeMap, rootNode, p);
        }
    }

    private void doIterationNoPruning() {
        HoldEmGameTree rootNode = HoldEmGameTree.getRandomRootState();
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            traverseMCCFR_NoPruning(nodeMap, rootNode, p);
        }
    }

    public void discount() {
        System.out.println("Now performing discount iteration " + numDiscounts);
        double discountValue = calculateDiscountValue(numDiscounts);
        nodeMap.discount(discountValue);
        numDiscounts++;
    }

    private static double calculateDiscountValue(int numDiscount) {
        return numDiscount / (numDiscount + 1);
    }

    public void loadFile(File file) throws IOException {
        this.file = file;
        nodeMap.loadFromFile(file);
    }
}
