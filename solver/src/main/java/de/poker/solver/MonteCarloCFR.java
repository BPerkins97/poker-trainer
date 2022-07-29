package de.poker.solver;

import de.poker.solver.map.persistence.FileSystem;
import de.poker.solver.game.Action;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.map.ActionMap;
import de.poker.solver.map.Strategy;

import java.util.List;
import java.util.Random;

// As implemented in http://www.cs.cmu.edu/~noamb/papers/19-Science-Superhuman_Supp.pdf
public class MonteCarloCFR {
    public static double traverseMCCFR_NoPruning(HoldEmGameTree state, int traversingPlayerId, Random random) {
        if (state.isGameOverForPlayer(traversingPlayerId)) {
            return state.getPayoffForPlayer(traversingPlayerId);
        } else {
            List<Action> actions = state.actions();
            if (state.isCurrentPlayer(traversingPlayerId)) {
                ActionMap node = FileSystem.getActionMap(state.toInfoSet());
                Strategy strategy = node.calculateStrategy(actions);
                for (Action action : actions) {
                    strategy.value(action, traverseMCCFR_NoPruning(state.takeAction(action), traversingPlayerId, random));
                }
                for (Action action : actions) {
                    node.addRegretForAction(action, (int)strategy.normalizedValue(action));
                }
                return strategy.expectedValue();
            } else {
                ActionMap node = FileSystem.getActionMap(state.toInfoSet());
                Strategy strategy = node.calculateStrategy(actions);
                Action action = strategy.randomAction(random);
                return traverseMCCFR_NoPruning(state.takeAction(action), traversingPlayerId, random);
            }
        }
    }

    public static double traverseMCCFR_WithPruning(HoldEmGameTree state, int traversingPlayerId, Random random) {
        if (state.isGameOverForPlayer(traversingPlayerId)) {
            return state.getPayoffForPlayer(traversingPlayerId);
        } else if (state.isCurrentPlayer(traversingPlayerId)) {
            List<Action> actions = state.actions();
            ActionMap node = FileSystem.getActionMap(state.toInfoSet());
            Strategy strategy = node.calculateStrategy(actions);
            for (Action action : actions) {
                if (node.regretForActionisAboveLimit(action, ApplicationConfiguration.MINIMUM_REGRET)) {
                    strategy.value(action, traverseMCCFR_WithPruning(state.takeAction(action), traversingPlayerId, random));
                    strategy.explored(action);
                }
            }
            for (Action action : actions) {
                if (strategy.hasBeenExplored(action)) {
                    node.addRegretForAction(action, (int) (strategy.normalizedValue(action)));
                }
            }
            return strategy.expectedValue();
        } else {
            ActionMap node = FileSystem.getActionMap(state.toInfoSet());
            Strategy strategy = node.calculateStrategy(state.actions());
            HoldEmGameTree nextState = state.takeAction(strategy.randomAction(random));
            return traverseMCCFR_WithPruning(nextState, traversingPlayerId, random);
        }
    }

    public static void updateStrategy(HoldEmGameTree state, int traversingPlayer, Random random) {
        if (state.isGameOverForPlayer(traversingPlayer) || !state.shouldUpdateRegrets()) {
            return;
        } else {
            List<Action> actions = state.actions();
            if (state.isCurrentPlayer(traversingPlayer)) {
                ActionMap node = FileSystem.getActionMap(state.toInfoSet());
                Strategy strategy = node.calculateStrategy(actions);
                Action chosenAction = strategy.randomAction(random);
                node.visitAction(chosenAction);
                updateStrategy(state.takeAction(chosenAction), traversingPlayer, random);
            } else {
                for (Action action : actions) {
                    updateStrategy(state.takeAction(action), traversingPlayer, random);
                }
            }
        }
    }
}
