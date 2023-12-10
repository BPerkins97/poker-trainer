package de.poker.trainer.solver.cfr;

import java.util.Arrays;

public final class MonteCarloCFR<ACTION, INFOSET> {
    private final GameFactory<ACTION, INFOSET> gameFactory;
    private final NodeMap<ACTION, INFOSET> nodeMap;
    private final int numPlayers;

    public MonteCarloCFR(NodeMap<ACTION, INFOSET> nodeMap, GameFactory<ACTION, INFOSET> gameFactory, int numPlayers) {
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
            for (int player=0;player<numPlayers;player++) {
                double[] expectedValue = cfr(game, startProbabilities, player);
                expectedValues[player] += expectedValue[player];
            }
            nodeMap.forEachNode(Node::updateStrategy);
        }

        for (int i=0;i<numPlayers;i++) {
            expectedValues[i] /= iterations;
        }
        return expectedValues;
    }

    private double[] cfr(Game<ACTION, INFOSET> game, double[] probabilities, int traversingPlayer) {
        if (game.isGameOver()) {
            return game.calculatePayoffs();
        }

        int currentPlayer = game.getCurrentPlayer();
        Node<ACTION> node = nodeMap.getNode(game);
        double[] actionUtility = new double[node.actions.length];
        double[] expectedValues = new double[node.actions.length];
        if (traversingPlayer == currentPlayer) {
            for (int action = 0; action < node.actions.length; action++) {
                Game<ACTION, INFOSET> nextGameState = game.takeAction(node.actions[action]);
                double[] nextProbabilities = calculateNextProbabilities(probabilities, currentPlayer, node, action);
                double[] result = cfr(nextGameState, nextProbabilities, traversingPlayer);
                actionUtility[action] = result[currentPlayer];
                for (int player = 0; player < numPlayers; player++) {
                    expectedValues[player] += result[player] * node.strategy[action];
                }
            }

            node.reachProbability = probabilities[currentPlayer];
            double accumulatedProbability = 1.0;
            for (int player = 0; player < numPlayers; player++) {
                if (player != currentPlayer) {
                    accumulatedProbability *= probabilities[player];
                }
            }

            for (int action = 0; action < node.actions.length; action++) {
                double regret = actionUtility[action] - expectedValues[currentPlayer];
                node.regretSum[action] += regret * accumulatedProbability;
            }

            nodeMap.update(game, node);
        } else {
            int actionIndex = node.pickRandomActionIndexAccordingToStrategy();
            double[] nextProbabilities = calculateNextProbabilities(probabilities, currentPlayer, node, actionIndex);
            return cfr(game.takeAction(node.actions[actionIndex]), nextProbabilities, traversingPlayer);
        }

        return expectedValues;
    }

    private static <ACTION> double[] calculateNextProbabilities(double[] probabilities, int currentPlayer, Node<ACTION> node, int actionIndex) {
        double[] nextProbabilities = Arrays.copyOf(probabilities, probabilities.length);
        nextProbabilities[currentPlayer] = nextProbabilities[currentPlayer] * node.strategy[actionIndex];
        return nextProbabilities;
    }
}
