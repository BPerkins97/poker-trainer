package de.poker.solver;

import de.poker.solver.game.Action;
import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.neural.NeuralNet;
import de.poker.solver.neural.Strategy;

public class MonteCarloCFR {
    public static double[] traverse(HoldEmGameTree state) {
        if (state.isGameOver()) {
            return HoldEmGameTree.getPayOffs(state);
        }
        Strategy strategy = NeuralNet.getStrategy(state);
        HoldEmGameTree[] nextStates = new HoldEmGameTree[strategy.actions.length];
        double[] regrets = new double[strategy.actions.length];
        double regretSum = 0;
        double[] totalPayOffs = new double[Constants.NUM_PLAYERS];
        for (int i = 0; i < strategy.actions.length; i++) {
            Action action = strategy.actions[i];
            nextStates[i] = state.takeAction(action);
            double[] payOffs = traverse(nextStates[i]);
            regrets[i] = payOffs[state.currentPlayer];
            for (int j = 0; j < Constants.NUM_PLAYERS; j++) {
                payOffs[j] *= strategy.probability[i];
                totalPayOffs[j] += payOffs[j];
            }
            regretSum += regrets[i];
        }

        for (int i = 0; i < strategy.actions.length; i++) {
            regrets[i] -= regretSum;
            NeuralNet.addTrainingData(nextStates[i], regrets[i]);
        }
        return totalPayOffs;
    }
}
