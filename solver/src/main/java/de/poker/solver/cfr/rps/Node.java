package de.poker.solver.cfr.rps;

public class Node {
    public double[] regretSum = new double[Solver.NUM_ACTIONS];
    public double[] strategySum = new double[Solver.NUM_ACTIONS];

    public double[] getStrategy() {
        double[] strategy = new double[Solver.NUM_ACTIONS];
        double normalizingSum = 0;
        for (int i=0;i<Solver.NUM_ACTIONS;i++) {
            strategy[i] = Math.max(regretSum[i], 0);
            normalizingSum += strategy[i];
        }
        for (int i=0;i<Solver.NUM_ACTIONS;i++) {
            if (normalizingSum > 0) {
                strategy[i] = strategy[i] / normalizingSum;
            } else {
                strategy[i] = 1.0 / Solver.NUM_ACTIONS;
            }
        }
        return strategy;
    }

    public double[] getAverageStrategy() {
        double[] averageStrategy = new double[Solver.NUM_ACTIONS];
        double normalizingSum = 0;
        for (int i=0;i<Solver.NUM_ACTIONS;i++) {
            normalizingSum += strategySum[i];
        }
        for (int i=0;i<Solver.NUM_ACTIONS;i++) {
            if (normalizingSum > 0) {
                averageStrategy[i] = strategySum[i] / normalizingSum;
            } else {
                averageStrategy[i] = 1.0 / Solver.NUM_ACTIONS;
            }
        }
        return averageStrategy;
    }
}
