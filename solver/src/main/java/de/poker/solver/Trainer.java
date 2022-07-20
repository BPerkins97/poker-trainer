package de.poker.solver;

import de.poker.solver.map.HoldEmNodeMap;

import java.sql.SQLException;

public class Trainer {

    private boolean isRunning = false;
    private HoldEmNodeMap nodeMap;
    public int iterations;

    public Trainer() throws SQLException {
        nodeMap = new HoldEmNodeMap();
    }

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
