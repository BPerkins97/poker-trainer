package de.poker.solver.pluribus.holdem;

import de.poker.solver.pluribus.MonteCarloCFR;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HoldEmRunner {
    private static final String ITERATION_PARAM = "-Diterations=";
    public static void main(String[] args) throws InterruptedException {
        int iterations = 1;
        for (String arg : args) {
            if (arg.startsWith(ITERATION_PARAM)) {
                iterations = Integer.parseInt(arg.split("=")[1]);
            }
        }
        HoldEmNodeMap nodeMap = new HoldEmNodeMap();
        int numCores = Runtime.getRuntime().availableProcessors();
        System.out.println("Running on n cores: " + numCores);
        System.out.println("Run for n iterations: " + iterations);
        ExecutorService executorService = Executors.newCachedThreadPool();
        MonteCarloCFR.mccfr_Pruning(executorService, new HoldEmConfiguration(), iterations, nodeMap);
        executorService.shutdown();
        executorService.awaitTermination(10L, TimeUnit.SECONDS);

        System.out.println(nodeMap);
    }
}
