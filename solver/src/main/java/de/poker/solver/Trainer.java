package de.poker.solver;

import de.poker.solver.map.HoldEmNodeMap;

import java.io.File;
import java.io.IOException;

public class Trainer {

    private volatile boolean isRunning = false;
    private HoldEmNodeMap nodeMap = new HoldEmNodeMap();
    public int iterations;


    public void start() {
        try {
            nodeMap.loadFromFile(new File("C:/Temp/tst.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            if (iterations == 5000) {
                try {
                    nodeMap.saveToFile(new File("C:/Temp/tst.txt"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } while (isRunning);
    }
}
