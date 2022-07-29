package de.poker.solver.map;

import de.poker.solver.ApplicationConfiguration;

public class Node {
    private int regret;
    private int regretGrowth;
    private short averageAction;

    public Node(int regret, short averageAction) {
        this.regret = Math.max(regret, ApplicationConfiguration.MINIMUM_REGRET);
        this.averageAction = averageAction;
    }

    public int getRegretGrowth() {
        return regretGrowth;
    }

    public int getRegret() {
        return regret;
    }


    public void incrementAverageAction() {
        averageAction++;
    }

    public short getAverageAction() {
        return averageAction;
    }

    public void addRegret(int regret) {
        this.regretGrowth += regret;
    }

    public void add(Node node) {
        this.regret += node.regretGrowth;
        this.averageAction += node.averageAction;
    }
}
