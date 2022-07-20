package de.poker.solver;

import de.poker.solver.map.HoldEmNodeMap;

public class Trainer {

    private boolean isRunning = false;
    private HoldEmNodeMap nodeMap = new HoldEmNodeMap();
    public int iterations;


    public void start() {
        isRunning = true;
        run();
    }

    public void stop() {
        isRunning = false;
    }

    private void run() {
        do {
            MonteCarloCFR.mccfr_Pruning(ApplicationConfiguration.RUN_ITERATIONS_AT_ONCE, nodeMap);
            iterations += ApplicationConfiguration.RUN_ITERATIONS_AT_ONCE;
        } while (isRunning);
    }
}
