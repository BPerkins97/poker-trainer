package de.poker.solver;

import de.poker.solver.database.NodeMap;
import de.poker.solver.game.Action;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.map.ActionMap;
import de.poker.solver.map.Strategy;

import java.util.List;
import java.util.Random;

// As implemented in http://www.cs.cmu.edu/~noamb/papers/19-Science-Superhuman_Supp.pdf
public class MonteCarloCFR {
    public static double traverseMCCFR_NoPruning(NodeMap nodeMap, HoldEmGameTree state, int traversingPlayerId, Random random) {
        if (state.isGameOverForPlayer(traversingPlayerId)) {
            return state.getPayoffForPlayer(traversingPlayerId);
        } else {
            List<Action> actions = state.actions();
            if (state.isCurrentPlayer(traversingPlayerId)) {
                ActionMap node = nodeMap.getActionMap(state);
                Strategy strategy = node.calculateStrategy(actions);
                for (Action action : actions) {
                    strategy.value(action, traverseMCCFR_NoPruning(nodeMap, state.takeAction(action), traversingPlayerId, random));
                }
                for (Action action : actions) {
                    node.addRegretForAction(action, (int)strategy.normalizedValue(action));
                }
                return strategy.expectedValue();
            } else {
                ActionMap node = nodeMap.getActionMap(state);
                Strategy strategy = node.calculateStrategy(actions);
                Action action = strategy.randomAction(random);
                return traverseMCCFR_NoPruning(nodeMap, state.takeAction(action), traversingPlayerId, random);
            }
        }
    }

    public static double traverseMCCFR_WithPruning(NodeMap nodeMap, HoldEmGameTree state, int traversingPlayerId, Random random) {
        if (state.isGameOverForPlayer(traversingPlayerId)) {
            return state.getPayoffForPlayer(traversingPlayerId);
        } else if (state.isCurrentPlayer(traversingPlayerId)) {
            List<Action> actions = state.actions();
            ActionMap node = nodeMap.getActionMap(state);
            Strategy strategy = node.calculateStrategy(actions);
            for (Action action : actions) {
                if (node.regretForActionisAboveLimit(action, ApplicationConfiguration.MINIMUM_REGRET)) {
                    strategy.value(action, traverseMCCFR_WithPruning(nodeMap, state.takeAction(action), traversingPlayerId, random));
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
            ActionMap node = nodeMap.getActionMap(state);
            Strategy strategy = node.calculateStrategy(state.actions());
            HoldEmGameTree nextState = state.takeAction(strategy.randomAction(random));
            return traverseMCCFR_WithPruning(nodeMap, nextState, traversingPlayerId, random);
        }
    }

    public static void updateStrategy(NodeMap nodeMap, HoldEmGameTree state, int traversingPlayer, Random random) {
        if (state.isGameOverForPlayer(traversingPlayer) || !state.shouldUpdateRegrets()) {
            return;
        } else {
            List<Action> actions = state.actions();
            if (state.isCurrentPlayer(traversingPlayer)) {
                ActionMap node = nodeMap.getActionMap(state);
                Strategy strategy = node.calculateStrategy(actions);
                Action chosenAction = strategy.randomAction(random);
                node.visitAction(chosenAction);
                updateStrategy(nodeMap, state.takeAction(chosenAction), traversingPlayer, random);
            } else {
                for (Action action : actions) {
                    updateStrategy(nodeMap, state.takeAction(action), traversingPlayer, random);
                }
            }
        }
    }
}
