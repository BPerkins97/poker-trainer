package de.poker.solver;

import java.io.File;
import java.util.Objects;

// TODO Multithreading
// TODO load data from checkpoint
// TODO write tests for everything
public class Debug {
    public static void main(String[] args) {
        Trainer trainer = new Trainer();
        trainer.file = new File("C:/Temp/tst.txt");
        trainer.start();
    }
}
