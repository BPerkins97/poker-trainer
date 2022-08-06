package kuhn.cfr;

public class Node {
    public double[] strategy;
    public double[] regretSum;
    public double[] strategySum;
    public int numActions;
    double reachProbability;
    double reachProbabilitySum;

    public Node(int numActions) {
        this.numActions = numActions;
        this.strategy = new double[numActions];
        this.regretSum = new double[numActions];
        this.strategySum = new double[numActions];
    }

    public void updateStrategy() {
        for (int i=0;i<numActions;i++) {
            strategySum[i] += strategy[i] * reachProbability;
        }
        reachProbabilitySum += reachProbability;
        reachProbability = 0;
        strategy = getStrategy();
    }

    public double[] getStrategy() {
        double normalizingSum = 0;
        for (int i=0;i<numActions;i++) {
            strategy[i] = Math.max(0, regretSum[i]);
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

    public double[] getAverageStrategy() {
        double[] averageStrategy = new double[numActions];
        double normalizingSum = 0;
        for (int i=0;i<numActions;i++) {
            averageStrategy[i] = strategySum[i] / reachProbabilitySum;
            normalizingSum += averageStrategy[i];
        }

        for (int i=0;i<numActions;i++) {
            averageStrategy[i] /= normalizingSum;
        }
        return averageStrategy;
    }
}
