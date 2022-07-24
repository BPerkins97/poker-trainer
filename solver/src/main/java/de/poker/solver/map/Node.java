package de.poker.solver.map;

import de.poker.solver.ApplicationConfiguration;

public class Node {
    private int regret;
    private int regretChange;
    private int averageAction;
    private long nodeId;

    public Node(int regret, int averageAction, long nodeId) {
        this.regret = Math.max(regret, ApplicationConfiguration.MINIMUM_REGRET);
        this.averageAction = averageAction;
        this.nodeId = nodeId;
    }

    public synchronized int getRegret() {
        return regret;
    }

    public int getRegretChange() {
        return regretChange;
    }

    public synchronized void addRegret(int regret) {
        this.regretChange += regret;
    }

    public synchronized void incrementAverageAction() {
        averageAction++;
    }

    public synchronized int getAverageAction() {
        return averageAction;
    }

    public long id() {
        return nodeId;
    }
}
