package de.poker.solver.cfr;

import de.poker.solver.cfr.kuhn.Hand;

import java.util.ArrayList;
import java.util.List;

public class ForProfiler {
    public static void main(String[] args) {
        PerformanceTest performanceTest = new PerformanceTest();
        List<Hand> hands = new ArrayList<>(10_000_000);
        for (int i=0;i<1_000_000;i++) {
            Hand hand = performanceTest.handTest();
            hands.add(hand);
        }
        System.out.println(hands);
    }
}
