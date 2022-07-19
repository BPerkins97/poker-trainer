package de.poker.solver;

import de.poker.solver.pluribus.MonteCarloCFR;
import de.poker.solver.pluribus.holdem.HoldEmConfiguration;
import de.poker.solver.pluribus.holdem.HoldEmNodeMap;

// TODO there is an issue where sometimes the values of a node are reset to zero for some reason this has to be an issue in the hashmap or?
public class Trainer {

    private boolean isRunning = false;
    private HoldEmConfiguration configuration = new HoldEmConfiguration();
    private HoldEmNodeMap nodeMap = new HoldEmNodeMap();


    public void start() {
        isRunning = true;
        run();
    }

    private void run() {
        do {
            MonteCarloCFR.mccfr_Pruning(configuration, ApplicationConfiguration.RUN_ITERATIONS_AT_ONCE, nodeMap);
        } while (isRunning);
    }
}
