package de.poker.solver;

import java.io.File;
import java.util.Objects;

// TODO Multithreading
// TODO persist data - no use of sql server its too damn slow, instead write to file every other hour or so
// TODO actions are determined based on config file which can be read at runtime
// TODO load data from checkpoint
// TODO Trainer does discounting
// TODO write tests for everything
// TODO maybe make stuff for functional
// TODO more efficient Hand Evaluation algorithm
// TODO maybe we find a way to map hole cards directly to an index of 169 and can reduce the map access cost like that
public class Main {
    public static void main(String[] args) {
        File file = null;
        for (int i=0;i<args.length;i++) {
            if (args[i].equals("--file")) {
                file = new File(args[i+1]);
                break;
            }
        }
        if (Objects.isNull(file)) {
            throw new IllegalArgumentException("Have to specify file");
        }
        Trainer trainer = new Trainer();
        trainer.file = file;
        trainer.start();
    }
}
