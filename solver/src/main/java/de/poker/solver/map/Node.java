package de.poker.solver.map;

import de.poker.solver.ApplicationConfiguration;

import java.util.Objects;

public class Node {
    private int regret;
    private int regretGrowth;
    private short averageAction;

    public Node(int regret, short averageAction) {
        this.regret = Math.max(regret, ApplicationConfiguration.MINIMUM_REGRET);
        this.averageAction = averageAction;
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
        this.regret = node.regret + regretGrowth;
        this.averageAction += node.averageAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return regret == node.regret && regretGrowth == node.regretGrowth && averageAction == node.averageAction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(regret, regretGrowth, averageAction);
    }

    @Override
    public String toString() {
        return "Node{" +
                "regret=" + regret +
                ", regretGrowth=" + regretGrowth +
                ", averageAction=" + averageAction +
                '}';
    }
}
