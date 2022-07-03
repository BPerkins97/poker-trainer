package de.poker.solver.cfr.kuhn;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Solver {
    public static final int NUM_PLAYERS = 6;

    Map<String, Node> nodeMap = new HashMap<>();
    Random random;

    public static void main(String[] args) {
        Solver solver = new Solver();
        solver.random = new Random(123L);
        solver.train(10);
        System.out.println(solver);
    }

    public void train(int iterations) {
        double[] expectedGameValue = new double[NUM_PLAYERS];
        for (int i=0;i<iterations;i++) {
            GameTreeNode gameTree = GameTreeNode.noLimitHoldEm6Max(random);
            double[] cfr = cfr(gameTree);
            for (int p=0;p<NUM_PLAYERS;p++) {
                expectedGameValue[p] += cfr[p];
            }
            nodeMap.values().forEach(Node::updateStrategy);
        }
        for (int p=0;p<NUM_PLAYERS;p++) {
            expectedGameValue[p] /= iterations;
            System.out.println(expectedGameValue[p]);
        }
    }

    public double[] cfr(GameTreeNode gameTree) {
        if (gameTree.isTerminal()) {
            return gameTree.getRewards();
        }

        Node node = getNode(gameTree);

        int currentPlayer = gameTree.currentPlayer();

        double[] strategy = node.strategy;
        double[][] actionUtility = new double[node.numActions][];

        for (int i=0;i<node.numActions;i++) {
            GameTreeNode nextNode = gameTree.takeAction(i, strategy[i]);
            actionUtility[i] = cfr(nextNode);
        }

        double[] utilitySum = new double[NUM_PLAYERS];
        for (int p=0;p<NUM_PLAYERS;p++) {
            for (int i=0;i<node.numActions;i++) {
                utilitySum[p] += actionUtility[i][p] * strategy[i];
            }
        }
        double[] regrets = new double[node.numActions];
        for (int i=0;i<node.numActions;i++) {
            regrets[i] = actionUtility[i][currentPlayer] - utilitySum[currentPlayer];
        }

        node.reachProbability += gameTree.reachProbability();

        for (int i=0;i<node.numActions;i++) {
            // TODO check if this makes a difference when the probability is gone
            double probabilty = gameTree.reachProbabiltyForRegret();
            node.regretSum[i] += probabilty * regrets[i];
        }
        return utilitySum;
    }

    private Node getNode(GameTreeNode gameTree) {
        String infoSet = gameTree.infoSet();
        if (!nodeMap.containsKey(infoSet)) {
            nodeMap.put(infoSet, gameTree.toNode());
        }
        return nodeMap.get(infoSet);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        nodeMap.values().forEach(node -> sb.append(node).append("\n"));
        return sb.toString();
    }
}
