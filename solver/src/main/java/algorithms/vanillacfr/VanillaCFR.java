package algorithms.vanillacfr;

import java.util.Arrays;

public final class VanillaCFR<ACTION, INFOSET> {
    private GameFactory<ACTION, INFOSET> gameFactory;
    private NodeMap<ACTION, INFOSET> nodeMap;
    private int numPlayers;

    public VanillaCFR(NodeMap<ACTION, INFOSET> nodeMap, GameFactory<ACTION, INFOSET> gameFactory, int numPlayers) {
        this.nodeMap = nodeMap;
        this.gameFactory = gameFactory;
        this.numPlayers = numPlayers;
    }

    public double[] run(int iterations) {
        double[] expectedValues = new double[numPlayers];
        for (int i=0;i<iterations;i++) {
            Game<ACTION, INFOSET> game = gameFactory.generate();
            double[] startProbabilities = new double[numPlayers];
            Arrays.fill(startProbabilities, 1);
            double[] expectedValue = cfr(game, startProbabilities);
            for (int j=0;j<numPlayers;j++) {
                expectedValues[j] += expectedValue[j];
            }
            nodeMap.forEachNode(Node::updateStrategy);
        }

        for (int i=0;i<numPlayers;i++) {
            expectedValues[i] /= iterations;
        }
        return expectedValues;
    }

    private double[] cfr(Game<ACTION, INFOSET> game, double[] probabilities) {
        if (game.isGameOver()) {
            return game.calculatePayoffs();
        }

        int currentPlayer = game.getCurrentPlayer();
        Node<ACTION> node = nodeMap.getNode(game);
        double[] expectedValueOfActions = new double[node.actions.length];
        double[] expectedValues = new double[node.actions.length];
        for (int i = 0; i < node.actions.length; i++) {
            Game<ACTION, INFOSET> nextGameState = game.takeAction(node.actions[i]);
            double[] nextProbabilities = Arrays.copyOf(probabilities, probabilities.length);
            nextProbabilities[currentPlayer] = nextProbabilities[currentPlayer] * node.strategy[i];
            double[] result = cfr(nextGameState, nextProbabilities);
            expectedValueOfActions[i] = result[currentPlayer];
            for (int j=0;j<probabilities.length;j++) {
                expectedValues[j] = result[j] * node.strategy[i];
            }
        }

        node.reachProbability = probabilities[currentPlayer];
        double accumulatedProbability = 1.0;
        for (int i=0;i<probabilities.length;i++) {
            if (i != currentPlayer) {
                accumulatedProbability *= probabilities[i];
            }
        }

        for (int i=0;i<node.actions.length;i++) {
            double regret = expectedValueOfActions[i] - expectedValues[currentPlayer];
            node.regretSum[i] += regret * accumulatedProbability;
        }

        nodeMap.update(game, node);

        return expectedValues;
    }
}
