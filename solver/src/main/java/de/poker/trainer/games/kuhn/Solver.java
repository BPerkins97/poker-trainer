package de.poker.trainer.games.kuhn;

import de.poker.trainer.solver.vanillacfr.InMemoryNodeMap;
import de.poker.trainer.solver.vanillacfr.VanillaCFR;

public class Solver {
    public static void main(String[] args) {
        InMemoryNodeMap<String, String> nodeMap = new InMemoryNodeMap<>();
        VanillaCFR<String, String> cfr = new VanillaCFR<>(nodeMap, new KuhnGameFactory(), 2);
        double[] run = cfr.run(10000000);
        System.out.println(nodeMap);
        System.out.println(run[0]);
        System.out.println(run[1]);
    }
}
