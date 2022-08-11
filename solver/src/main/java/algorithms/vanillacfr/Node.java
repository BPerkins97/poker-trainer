package algorithms.vanillacfr;

public class Node<A> {
    double[] strategy;
    double[] regretSum;
    double[] strategySum;
    A[] actions;
    double reachProbability;
    double reachProbabilitySum;

    public Node(A[] actions) {
        this.actions = actions;
        this.strategy = new double[actions.length];
        this.regretSum = new double[actions.length];
        this.strategySum = new double[actions.length];
    }

    public void updateStrategy() {
        for (int i=0;i<actions.length;i++) {
            strategySum[i] += strategy[i] * reachProbability;
        }
        reachProbabilitySum += reachProbability;
        reachProbability = 0;
        strategy = getStrategy();
    }

    public double[] getStrategy() {
        double normalizingSum = 0;
        for (int i=0;i<actions.length;i++) {
            strategy[i] = Math.max(0, regretSum[i]);
            normalizingSum += strategy[i];
        }

        for (int i=0;i<actions.length;i++) {
            if (normalizingSum > 0) {
                strategy[i] /= normalizingSum;
            } else {
                strategy[i] = 1.0 / actions.length;
            }
        }
        return strategy;
    }

    public double[] getAverageStrategy() {
        double[] averageStrategy = new double[actions.length];
        double normalizingSum = 0;
        for (int i=0;i<actions.length;i++) {
            averageStrategy[i] = strategySum[i] / reachProbabilitySum;
            normalizingSum += averageStrategy[i];
        }

        for (int i=0;i<actions.length;i++) {
            averageStrategy[i] /= normalizingSum;
        }
        return averageStrategy;
    }
}
