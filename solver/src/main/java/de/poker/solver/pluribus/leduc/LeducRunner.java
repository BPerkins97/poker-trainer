package de.poker.solver.pluribus.leduc;

import de.poker.solver.pluribus.MonteCarloCFR;

public class LeducRunner {

    public static void main(String[] args) {
        LeducNodeMap nodeMap = new LeducNodeMap();
        MonteCarloCFR.mccfr_Pruning(new LeducConfiguration(), 1000000, nodeMap);
        System.out.println(nodeMap);
    }
}
