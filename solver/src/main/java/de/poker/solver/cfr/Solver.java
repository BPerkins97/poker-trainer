package de.poker.solver.cfr;

import de.poker.solver.game.GameTreeNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static de.poker.solver.game.GameTreeNode.NUM_PLAYERS;

public class Solver {
    Map<String, Node> nodeMap = new HashMap<>();

    public double[] train(int iterations, Random random) {
        double[] expectedGameValue = new double[NUM_PLAYERS];
        for (int i = 0; i < iterations; i++) {
            GameTreeNode gameTree = GameTreeNode.headsUpNoLimitHoldEm(random);
            double[] cfr = cfr(gameTree);
            for (int p = 0; p < NUM_PLAYERS; p++) {
                expectedGameValue[p] += cfr[p];
            }
            nodeMap.values().forEach(node -> {
                if (node.touched) {
                    node.updateStrategy();
                }
            });
        }
        return expectedGameValue;
    }

    public double[] cfr(GameTreeNode gameTree) {
        if (gameTree.isTerminal()) {
            return gameTree.getRewards();
        }

        Node node = getNode(gameTree);

        int currentPlayer = gameTree.currentPlayer();

        double[] strategy = node.strategy;
        double[][] actionUtility = new double[node.numActions][];

        for (int i = 0; i < node.numActions; i++) {
            if  (strategy[i] > 0) {
                GameTreeNode nextNode = gameTree.takeAction(i, strategy[i]);
                actionUtility[i] = cfr(nextNode);
            }
        }

        double[] utilitySum = new double[NUM_PLAYERS];
        for (int p = 0; p < NUM_PLAYERS; p++) {
            for (int i = 0; i < node.numActions; i++) {
                utilitySum[p] += actionUtility[i][p] * strategy[i];
            }
        }
        double[] regrets = new double[node.numActions];
        for (int i = 0; i < node.numActions; i++) {
            regrets[i] = actionUtility[i][currentPlayer] - utilitySum[currentPlayer];
        }

        node.reachProbability += gameTree.reachProbability();

        for (int i = 0; i < node.numActions; i++) {
            double probabilty = gameTree.reachProbabiltyForRegret();
            regrets[i] += probabilty * regrets[i];
        }
        node.updateRegrets(regrets);
        return utilitySum;
    }

    private Node getNode(GameTreeNode gameTree) {
        String infoSet = gameTree.infoSet();
        if (!nodeMap.containsKey(infoSet)) {
            nodeMap.put(infoSet, gameTree.toNode(infoSet));
        }
        Node node = nodeMap.get(infoSet);
        node.touched = true;
        return node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        nodeMap.values().forEach(node -> sb.append(node).append("\n"));
        return sb.toString();
    }
}
