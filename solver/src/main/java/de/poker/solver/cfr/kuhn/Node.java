package de.poker.solver.cfr.kuhn;

public class Node {
    String key;
    String[] actionDictionary;
    int numActions;
    double[] regretSum;
    double[] strategySum;
    double[] strategy;
    double reachProbability;
    double reachProbabilitySum;

    public Node(String key, String[] actionDictionary) {
        this.actionDictionary = actionDictionary;
        this.numActions = actionDictionary.length;
        this.key = key;
        regretSum = new double[this.numActions];
        strategySum = new double[this.numActions];
        reachProbability = 0;
        reachProbabilitySum = 0;
        strategy = new double[this.numActions];
        for (int i=0;i<this.numActions;i++) {
            strategy[i] = 1.0 / this.numActions;
        }
    }

    public void updateStrategy() {
        for (int i=0;i<numActions;i++) {
            strategySum[i] += strategy[i] * reachProbability;
        }
        reachProbabilitySum += reachProbability;
        strategy = getStrategy();
        reachProbability = 0;
    }

    public double[] getStrategy() {
        double normalizingSum = 0;
        double[] strategy = new double[numActions];
        for (int i=0;i<numActions;i++) {
            strategy[i] = Math.max(regretSum[i], 0);
            normalizingSum += strategy[i];
        }
        for (int i=0;i<numActions;i++) {
            if (normalizingSum > 0) {
                strategy[i] = strategy[i] / normalizingSum;
            } else {
                strategy[i] = 1.0 / numActions;
            }
        }
        return strategy;
    }

    public double[] getAverageStrategy() {
        double[] strategy = new double[numActions];
        double normalizingSum = 0;
        for (int i=0;i<numActions;i++) {
            strategy[i] = strategySum[i] / reachProbabilitySum;
            normalizingSum += strategy[i];
        }
        for (int i=0;i<numActions;i++) {
            if (normalizingSum > 0) {
                strategy[i] /= normalizingSum;
            } else {
                strategy[i] = 1.0 / numActions;
            }
        }
        return strategy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append("\t\t");
        double[] avgStrategy = getAverageStrategy();
        for (int i=0;i<numActions;i++) {
            sb.append(actionDictionary[i]);
            sb.append(":");
            sb.append(String.format("%.2f", avgStrategy[i]));
            sb.append("\t");
        }
        return sb.toString();
    }
}
