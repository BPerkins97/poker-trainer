package de.poker.solver.pluribus.holdem;

import de.poker.solver.pluribus.MonteCarloCFR;
import de.poker.solver.pluribus.Node;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HoldEmRunner {

    public static void main(String[] args) {
        HoldEmNodeMap nodeMap = new HoldEmNodeMap();
        MonteCarloCFR.mccfr_Pruning(new HoldEmConfiguration(), 1000, nodeMap);
        System.out.println(nodeMap);
        List<Map.Entry<String, Node>> collect = nodeMap.map.entrySet().stream()
                .filter(entry -> entry.getKey().length() <= 5)
                .collect(Collectors.toList());
        System.out.println(collect);
    }
}
