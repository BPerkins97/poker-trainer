package de.poker.solver.map;

import de.poker.solver.ApplicationConfiguration;

public class Node implements NodeInterface {
    private int regret;
    private short averageAction;

    public Node(int regret, short averageAction) {
        this.regret = Math.max(regret, ApplicationConfiguration.MINIMUM_REGRET);
        this.averageAction = averageAction;
    }

    @Override
    public int getRegret() {
        return regret;
    }

    @Override
    public void setRegret(int regret) {
        this.regret = regret;
    }

    public void incrementAverageAction() {
        averageAction++;
    }

    @Override
    public short getAverageAction() {
        return averageAction;
    }

    @Override
    public void setAverageAction(short averageAction) {
        this.averageAction = averageAction;
    }

    public void addRegret(int regret) {
        this.regret += regret;
    }
}
