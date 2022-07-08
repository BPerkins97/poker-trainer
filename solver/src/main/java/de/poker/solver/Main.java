package de.poker.solver;

import de.poker.solver.cfr.Solver;

import java.util.Random;

public class Main {

    public static void main(String[] args) {
        Solver solver = new Solver();
        solver.train(5, new Random(123L));
        System.out.println("Done");
    }
}
