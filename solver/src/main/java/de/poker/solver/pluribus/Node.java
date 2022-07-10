package de.poker.solver.pluribus;

import java.util.Arrays;

public class Node {
    private int[] regrets;
    private int[] averageAction;

    public double[] calculateStrategy() {
        double sum = 0;
        for (int a=0;a<regrets.length;a++) {
            sum += regrets[a];
        }
        double[] strategy = new double[regrets.length];
        for (int a=0;a<regrets.length;a++) {
            if (sum > 0) {
                strategy[a] = regrets[a] / sum;
            } else {
                strategy[a] = (double) regrets[a] / regrets.length;
            }
        }
        return strategy;
    }

    public void visitAction(int action) {
        averageAction[action]++;
    }

    public void addRegretForAction(int action, double regret) {
        regrets[action] += regret;
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
