package de.poker.trainer.solver.cfr;

import java.util.Random;

public class Node<A> {
    private static final Random RANDOM = new Random();
    final double[] strategy;
    final double[] regretSum;
    final double[] strategySum;
    final A[] actions;

    public Node(A[] actions) {
        this.actions = actions;
        this.strategy = new double[actions.length];
        this.regretSum = new double[actions.length];
        this.strategySum = new double[actions.length];
        updateStrategy();
    }

    public double[] getAverageStrategy() {
        double[] averageStrategy = new double[actions.length];
        double normalizingSum = 0;
        for (int i=0;i<actions.length;i++) {
            averageStrategy[i] = strategySum[i];
            normalizingSum += averageStrategy[i];
        }

        for (int i=0;i<actions.length;i++) {
            if (normalizingSum > 0) {
                averageStrategy[i] /= normalizingSum;
            } else {
                averageStrategy[i] = 1.0 / actions.length;
            }
        }
        return averageStrategy;
    }

    @Override
    public String toString() {
        double[] averageStrategy = getAverageStrategy();
        StringBuilder builder = new StringBuilder();
        for (int i=0;i<averageStrategy.length;i++) {
            builder
                    .append(String.format("%s: %.4f \t", actions[i], averageStrategy[i]));
        }
        return builder.toString();
    }

    public int pickRandomActionIndexAccordingToStrategy() {
        double randomValue = RANDOM.nextDouble();
        double accumulated = 0;
        for (int i=0;i<actions.length;i++) {
            accumulated += strategy[i];
            if (randomValue < accumulated) {
                return i;
            }
        }
        throw new IllegalStateException("Should not be able to reach this point!");
    }

    public void updateRegrets(double[] actionUtilities, int chosenAction, double reachProbability) {
        for (int i=0;i<actions.length;i++) {
            double regret = actionUtilities[i] - actionUtilities[chosenAction];
            regretSum[i] += regret * reachProbability;
        }
        updateStrategy();
    }

    private void updateStrategy() {
        double normalizingSum = 0;
        for (int i=0;i<actions.length;i++) {
            strategy[i] = Math.max(0, regretSum[i]);
            normalizingSum += strategy[i];
        }

        for (int i=0;i<actions.length;i++) {
            if (normalizingSum > 0) {
                strategy[i] /= normalizingSum;
                strategySum[i] += strategy[i];
            } else {
                strategy[i] = 1.0 / actions.length;
            }
        }
    }
}
