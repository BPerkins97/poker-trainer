package de.poker.solver;

import de.poker.solver.map.HoldEmNodeMap;

import java.io.File;
import java.io.IOException;

public class Trainer {
    private volatile boolean isRunning = false;
    private HoldEmNodeMap nodeMap = new HoldEmNodeMap();
    public int iterations;
    public File file;
    private int numDiscounts;
    private long nextDiscount = 0;


    public void start() {
        try {
            if (file.exists()) {
                nodeMap.loadFromFile(file);
                nextDiscount = calculateNextDiscountTimestamp();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            MonteCarloCFR.mccfr_Pruning(ApplicationConfiguration.RUN_ITERATIONS_AT_ONCE, nodeMap);
            iterations += ApplicationConfiguration.RUN_ITERATIONS_AT_ONCE;
            if (iterations < ApplicationConfiguration.NUM_DISCOUNT_THRESHOLD && System.currentTimeMillis() > nextDiscount) {
                discount();
            }
        } while (isRunning);
        try {
            nodeMap.saveToFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
}
