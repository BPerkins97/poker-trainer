package de.poker.solver.pluribus.kuhn;

import de.poker.solver.pluribus.MonteCarloCFR;

public class KuhnRunner {

    public static void main(String[] args) {
        KuhnNodeMap nodeMap = new KuhnNodeMap();
        MonteCarloCFR.mccfr_Pruning(new KuhnConfiguration(), 1000, nodeMap);
        System.out.println(nodeMap);
    }
}
