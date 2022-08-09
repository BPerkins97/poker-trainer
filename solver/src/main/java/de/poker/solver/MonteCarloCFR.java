package de.poker.solver;

import de.poker.solver.game.Action;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.neural.NeuralNet;
import de.poker.solver.neural.Strategy;

public class MonteCarloCFR {
    public static double[] traverse(HoldEmGameTree state) {
        if (state.isGameOver()) {
            return HoldEmGameTree.getPayOffs(state);
        }
        Strategy strategy = NeuralNet.getStrategy(state);
        double maxEV = Double.MIN_VALUE;
        Action maxEvAction = null;
        for (int i = 0; i<strategy.expectedValues.length; i++) {
            if (maxEV < strategy.expectedValues[i]) {
                maxEV = strategy.expectedValues[i];
                maxEvAction = strategy.actions[i];
            }
        }
        assert maxEvAction != null;

        HoldEmGameTree nextState = state.takeAction(maxEvAction);
        double[] payOffs = traverse(nextState);
        NeuralNet.addTrainingData(nextState, payOffs[state.currentPlayer]);
        return payOffs;
    }
}
