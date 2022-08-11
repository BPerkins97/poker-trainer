package holdem.cap.headsup.preflop;

import de.poker.solver.game.Action;

import java.util.Arrays;
import java.util.Map;

public class VanillaCFR {

    public static double[] cfr(Map<String, Node> nodeMap, Game game, double[] reachProbabilities, double[] regretSum) {
        if (game.isGameOver()) {
            int[] payoffs = game.getPayoffs();
            double[] r = new double[2];
            for (int i=0;i<r.length;i++) {
                r[i] = 1.0 * payoffs[i];
            }
            return r;
        }

        String infoSet = game.getInfoSetOfGame();
        if (!nodeMap.containsKey(infoSet)) {
            Action[] possibleActions = game.getLegalActions();
            nodeMap.put(infoSet, new Node(possibleActions));
        }
        Node node = nodeMap.get(infoSet);
        double[] strategy = node.getStrategy();
        Action[] actions = node.getActions();
        double[] expectedValueOfActions = new double[actions.length];
        double[] expectedValues = new double[2];
        for (int i=0;i<actions.length;i++) {
            Game nextGameState = game.takeAction(actions[i]);
            double[] nextReachProbabilities = Arrays.copyOf(reachProbabilities, reachProbabilities.length);
            nextReachProbabilities[game.currentPlayer()] = reachProbabilities[game.currentPlayer()] * strategy[game.currentPlayer()];
            double[] expectedValue = cfr(nodeMap, nextGameState, nextReachProbabilities, regretSum);
            expectedValueOfActions[game.currentPlayer()] = expectedValue[game.currentPlayer()];
            for (int j=0;j<2;j++) {
                expectedValues[j] += expectedValue[j] * strategy[i];
            }
        }

        node.reachProbability += reachProbabilities[game.currentPlayer()];
        double accumulatedReachProbability = 1.0;
        for (int i=0;i<2;i++) {
            if (i != game.currentPlayer()) {
                accumulatedReachProbability *= reachProbabilities[i];
            }
        }
        for (int i=0;i<actions.length;i++) {
            double regret = expectedValueOfActions[i] - expectedValues[game.currentPlayer()];
            regretSum[0] += regret;
            node.regretSum[i] += regret * accumulatedReachProbability;
        }
        return expectedValues;
    }
}
