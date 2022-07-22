package de.poker.solver.map;

import de.poker.solver.ApplicationConfiguration;

public class Node {
    private int regret;
    private int averageAction;

    public Node(int regret, int averageAction) {
        this.regret = regret;
        this.averageAction = averageAction;
    }

    public synchronized int getRegret() {
        return regret;
    }

    public synchronized void addRegret(int regret) {
        this.regret = Math.max(regret + this.regret, ApplicationConfiguration.MINIMUM_REGRET);
    }

    public synchronized void incrementAverageAction() {
        averageAction++;
    }

    public synchronized int getAverageAction() {
        return averageAction;
    }

    public synchronized void discount(double discountValue) {
        this.regret += discountValue;
    }
}
