package de.poker.trainer.solver.cfr;

import de.poker.trainer.math.MathUtils;

import java.util.Arrays;

public final class VanillaCFR<ACTION, INFOSET> {
    private final GameFactory<ACTION, INFOSET> gameFactory;
    private final NodeMap<ACTION, INFOSET> nodeMap;
    private final int numPlayers;

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
            for (int player=0;player<numPlayers;player++) {
                expectedValues[player] += expectedValue[player];
            }
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
        double[] actionUtility = new double[node.actions.length];
        double[] expectedValues = new double[node.actions.length];
        for (int action = 0; action < node.actions.length; action++) {
            Game<ACTION, INFOSET> nextGameState = game.takeAction(node.actions[action]);
            double[] nextProbabilities = Arrays.copyOf(probabilities, probabilities.length);
            nextProbabilities[currentPlayer] = nextProbabilities[currentPlayer] * node.strategy[action];
            double[] result = cfr(nextGameState, nextProbabilities);
            actionUtility[action] = result[currentPlayer];
            for (int player=0;player<numPlayers;player++) {
                expectedValues[player] += result[player] * node.strategy[action];
            }
        }

        double accumulatedProbability = MathUtils.product(probabilities) / (probabilities[currentPlayer] == 0 ? 1 : probabilities[currentPlayer]);

        node.updateRegrets(actionUtility, accumulatedProbability);

        nodeMap.update(game, node);
        return expectedValues;
    }
}
