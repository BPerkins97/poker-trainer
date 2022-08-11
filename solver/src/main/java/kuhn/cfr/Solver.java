package kuhn.cfr;

import algorithms.vanillacfr.InMemoryNodeMap;
import algorithms.vanillacfr.VanillaCFR;

public class Solver {
    public static void main(String[] args) {
        VanillaCFR<String, String> cfr = new VanillaCFR<>(new InMemoryNodeMap<>(), new KuhnGameFactory(), 2);
        double[] run = cfr.run(1000000);
        System.out.println(run[0]);
    }
}
