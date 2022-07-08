package de.poker.solver.cfr.kuhn;

public class Node {
    String key;
    String[] actionDictionary;
    int numActions;
    int numCards;
    double[][] regretSum;
    double[][] strategySum;
    double[][] strategy;
    double[] reachProbability;
    double[] reachProbabilitySum;

    public Node(String key, String[] actionDictionary, int numCards) {
        this.actionDictionary = actionDictionary;
        this.numActions = actionDictionary.length;
        this.numCards = numCards;
        this.key = key;
        regretSum = new double[this.numCards][];
        strategySum = new double[this.numCards][];
        reachProbability = new double[numCards];
        reachProbabilitySum = new double[numCards];
        strategy = new double[this.numCards][];
        for (int c=0;c<numCards;c++) {
            regretSum[c] = new double[numActions];
            strategySum[c] = new double[numActions];
            strategy[c] = new double[numActions];
            for (int a=0;a<numActions;a++) {
                strategy[c][a] = 1.0 / this.numActions;
            }
        }
    }

    public void updateStrategy() {
        for (int c=0;c<numCards;c++) {
            for (int a=0;a<numActions;a++) {
                strategySum[c][a] += strategy[c][a] * reachProbability[c];
            }
            reachProbabilitySum[c] += reachProbability[c];
            reachProbability[c] = 0;

        }
        strategy = getStrategy();
    }

    private double[][] getStrategy() {
        double[][] strategy = new double[numCards][];
        for (int c=0;c<numCards;c++) {
            strategy[c] = new double[numActions];
            double normalizingSum = 0;
            for (int a=0;a<numActions;a++) {
                strategy[c][a] = Math.max(regretSum[c][a], 0);
                normalizingSum += strategy[c][a];
            }
            for (int a=0;a<numActions;a++) {
                if (normalizingSum > 0) {
                    strategy[c][a] = strategy[c][a] / normalizingSum;
                } else {
                    strategy[c][a] = 1.0 / numActions;
                }
            }
        }
        return strategy;
    }

    public double[][] getAverageStrategy() {
        double[][] strategy = new double[numCards][];
        for (int c=0;c<numCards;c++) {
            double normalizingSum = 0;
            strategy[c] = new double[numActions];
            for (int a=0;a<numActions;a++) {
                strategy[c][a] = strategySum[c][a] / reachProbabilitySum[c];
                normalizingSum += strategy[c][a];
            }
            for (int a=0;a<numActions;a++) {
                if (normalizingSum > 0) {
                    strategy[c][a] /= normalizingSum;
                } else {
                    strategy[c][a] = 1.0 / numActions;
                }
            }
        }
        return strategy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key);
        sb.append("\t\t");
        double[][] avgStrategy = getAverageStrategy();
        for (int c=0;c<numCards;c++) {
            sb.append(c);
            for (int i=0;i<numActions;i++) {
                sb.append(String.format("%.2f", avgStrategy[c][i]));
                sb.append("\t");
            }
        }
        return sb.toString();
    }

    public void addProbability(double[] probability) {
        for (int c=0;c<numCards;c++) {
            reachProbability[c] += probability[c];
        }
    }

    public void addRegrets(double[][] regrets, double[] probability) {
        for (int c=0;c<numCards;c++) {
            for (int a=0;a<numActions;a++) {
                regretSum[c][a] += regrets[c][a] * probability[c];
            }
        }
    }
}
