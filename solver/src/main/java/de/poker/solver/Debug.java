package de.poker.solver;

import java.io.File;
import java.util.Objects;

// TODO Multithreading
// TODO actions are determined based on config file which can be read at runtime
// TODO load data from checkpoint
// TODO write tests for everything
// TODO maybe make stuff for functional
// TODO more efficient Hand Evaluation algorithm
// TODO maybe we find a way to map hole cards directly to an index of 169 and can reduce the map access cost like that
public class Debug {
    public static void main(String[] args) {
        Trainer trainer = new Trainer();
        trainer.file = new File("C:/Temp/tst.txt");
        trainer.start();
    }
}
