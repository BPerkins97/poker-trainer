package de.poker.solver;

import de.poker.solver.game.Action;
import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.map.ActionMap;
import de.poker.solver.map.HoldEmNodeMap;
import de.poker.solver.map.Strategy;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// As implemented in http://www.cs.cmu.edu/~noamb/papers/19-Science-Superhuman_Supp.pdf
public class MonteCarloCFR {

    public static HoldEmNodeMap mccfr_Pruning(int iterations, HoldEmNodeMap nodeMap) {
        for (int i = 0; i < iterations; i++) {
            HoldEmGameTree rootNode = HoldEmGameTree.getRandomRootState();
            double randomNumber = ThreadLocalRandom.current().nextDouble();
            final int iteration = i;
            doIteration(nodeMap, iteration, rootNode, randomNumber);
            if (i < ApplicationConfiguration.LINEAR_CFR_THRESHOLD && i % ApplicationConfiguration.DISCOUNT_INTERVAL == 0 && i >= ApplicationConfiguration.DISCOUNT_INTERVAL) {
                double discountValue = calculateDiscountValue(i);
                nodeMap.discount(discountValue);
            }
        }
        return nodeMap;
    }

    private static void doIteration(HoldEmNodeMap nodeMap, int i, HoldEmGameTree rootNode, double randomNumber) {
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            if (i > ApplicationConfiguration.PRUNING_THRESHOLD) {
                if (randomNumber < 0.05) {
                    traverseMCCFR_NoPruning(nodeMap, rootNode, p);
                } else {
                    traverseMCCFR_WithPruning(nodeMap, rootNode, p);
                }
                if (i % ApplicationConfiguration.STRATEGY_INTERVAL == 0) {
                    updateStrategy(nodeMap, rootNode, p);
                }
            } else {
                traverseMCCFR_NoPruning(nodeMap, rootNode, p);
            }
        }
    }

    private static double calculateDiscountValue(int iteration) {
        double temp = (double) iteration / ApplicationConfiguration.DISCOUNT_INTERVAL;
        return temp / (temp + 1);
    }

    private static double traverseMCCFR_NoPruning(HoldEmNodeMap nodeMap, HoldEmGameTree state, int traversingPlayerId) {
        if (state.isGameOverForPlayer(traversingPlayerId)) {
            return state.getPayoffForPlayer(traversingPlayerId);
        } else {
            List<Action> actions = state.actions();
            if (state.isCurrentPlayer(traversingPlayerId)) {
                ActionMap node = nodeMap.getNodeForCurrentPlayer(state);
                Strategy strategy = node.calculateStrategy(actions);
                for (Action action : actions) {
                    strategy.value(action, traverseMCCFR_NoPruning(nodeMap, state.takeAction(action), traversingPlayerId));
                }
                for (Action action : actions) {
                    node.addRegretForAction(action, (int)strategy.normalizedValue(action));
                }
                nodeMap.updateForCurrentPlayer(state, node);
                return strategy.expectedValue();
            } else {
                ActionMap node = nodeMap.getNodeForCurrentPlayer(state);
                Strategy strategy = node.calculateStrategy(actions);
                Action action = strategy.randomAction();
                return traverseMCCFR_NoPruning(nodeMap, state.takeAction(action), traversingPlayerId);
            }
        }
    }

    private static double traverseMCCFR_WithPruning(HoldEmNodeMap nodeMap, HoldEmGameTree state, int traversingPlayerId) {
        if (state.isGameOverForPlayer(traversingPlayerId)) {
            return state.getPayoffForPlayer(traversingPlayerId);
        } else if (state.isCurrentPlayer(traversingPlayerId)) {
            List<Action> actions = state.actions();
            ActionMap node = nodeMap.getNodeForCurrentPlayer(state);
            Strategy strategy = node.calculateStrategy(actions);
            for (Action action : actions) {
                if (node.regretForActionisAboveLimit(action, ApplicationConfiguration.MINIMUM_REGRET)) {
                    strategy.value(action, traverseMCCFR_WithPruning(nodeMap, state.takeAction(action), traversingPlayerId));
                    strategy.explored(action);
                }
            }
            for (Action action : actions) {
                if (strategy.hasBeenExplored(action)) {
                    node.addRegretForAction(action, (int) (strategy.normalizedValue(action)));
                }
            }
            nodeMap.updateForCurrentPlayer(state, node);
            return strategy.expectedValue();
        } else {
            ActionMap node = nodeMap.getNodeForCurrentPlayer(state);
            Strategy strategy = node.calculateStrategy(state.actions());
            HoldEmGameTree nextState = state.takeAction(strategy.randomAction());
            return traverseMCCFR_WithPruning(nodeMap, nextState, traversingPlayerId);
        }
    }

    private static void updateStrategy(HoldEmNodeMap nodeMap, HoldEmGameTree state, int traversingPlayer) {
        if (state.isGameOverForPlayer(traversingPlayer) || !state.shouldUpdateRegrets()) {
            return;
        } else {
            List<Action> actions = state.actions();
            if (state.isCurrentPlayer(traversingPlayer)) {
                ActionMap node = nodeMap.getNodeForCurrentPlayer(state);
                Strategy strategy = node.calculateStrategy(actions);
                Action chosenAction = strategy.randomAction();
                node.visitAction(chosenAction);
                updateStrategy(nodeMap, state.takeAction(chosenAction), traversingPlayer);
            } else {
                for (Action action : actions) {
                    updateStrategy(nodeMap, state.takeAction(action), traversingPlayer);
                }
            }
        }
    }
}
