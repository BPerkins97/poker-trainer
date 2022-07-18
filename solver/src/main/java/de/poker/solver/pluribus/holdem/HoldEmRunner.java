package de.poker.solver.pluribus.holdem;

import de.poker.solver.pluribus.MonteCarloCFR;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HoldEmRunner {
    public static void main(String[] args) throws InterruptedException {
        HoldEmNodeMap nodeMap = new HoldEmNodeMap();
        int numCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Running on n cores: " + numCores);
        ExecutorService executorService = Executors.newCachedThreadPool();
        MonteCarloCFR.mccfr_Pruning(executorService, new HoldEmConfiguration(), 1000, nodeMap);
        executorService.shutdown();
        executorService.awaitTermination(10L, TimeUnit.SECONDS);

        System.out.println(nodeMap);
    }
}
