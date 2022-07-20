package de.poker.solver;

import de.poker.solver.game.HoldEmGameTree;

public class Node {
    private int[] regrets;
    private int[] averageAction;

    public Node(HoldEmGameTree state) {
        regrets = new int[state.numActions()];
        averageAction = new int[state.numActions()];
    }
    public double[] calculateStrategy() {
        double sum = 0;
        double[] strategy = new double[regrets.length];
        for (int a=0;a<regrets.length;a++) {
            strategy[a] = Math.max(0, regrets[a]);
            sum += strategy[a];
        }
        for (int a=0;a<regrets.length;a++) {
            if (sum > 0) {
                strategy[a] = strategy[a] / sum;
            } else {
                strategy[a] = 1.0 / regrets.length;
            }
        }
        return strategy;
    }

    public void visitAction(int action) {
        averageAction[action]++;
    }

    public void addRegretForAction(int action, int regret) {
        regrets[action] = Math.max(regrets[action] + regret, ApplicationConfiguration.MINIMUM_REGRET);
    }

    public void discount(double discountValue) {
        for (int i=0;i<regrets.length;i++) {
            regrets[i] *= discountValue;
        }
    }

    public boolean regretForActionisAboveLimit(int action, int limit) {
        return regrets[action] > limit;
    }
}
