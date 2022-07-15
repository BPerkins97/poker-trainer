package de.poker.solver.pluribus.holdem;

import de.poker.solver.pluribus.MonteCarloCFR;

public class HoldEmRunner {

    public static void main(String[] args) {
        HoldEmNodeMap nodeMap = new HoldEmNodeMap();
        MonteCarloCFR.mccfr_Pruning(new HoldEmConfiguration(), 1000, nodeMap);
        System.out.println(nodeMap);
    }
}
