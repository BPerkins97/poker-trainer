package de.poker.solver.pluribus;

import java.util.Arrays;

public class Node {
    private int[] regrets;
    private int[] averageAction;

    public Node(int numActions) {
        regrets = new int[numActions];
        averageAction = new int[numActions];
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

    public void addRegretForAction(int action, int regret, Configuration config) {
        regrets[action] = Math.max(regrets[action] + regret, config.minimumRegret());
    }

    public void resetRegrets() {
        Arrays.fill(regrets, 0);
    }

    public void resetAverageStrategy() {
        Arrays.fill(averageAction, 0);
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
