package de.poker.solver;

import java.io.File;
import java.util.Objects;

// TODO Multithreading
// TODO load data from checkpoint
// TODO write tests for everything
public class Debug {
    public static void main(String[] args) {
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(50, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(100, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(150, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[1].add(new BetSize(100, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[2].add(new BetSize(100, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[3].add(new BetSize(100, BetSize.PERCENT));
        Trainer trainer = new Trainer();
        trainer.file = new File("C:/Temp/tst.txt");
        trainer.start();
    }
}
