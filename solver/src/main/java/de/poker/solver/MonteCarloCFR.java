package de.poker.solver;

import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;

import java.util.concurrent.ThreadLocalRandom;

// As implemented in http://www.cs.cmu.edu/~noamb/papers/19-Science-Superhuman_Supp.pdf
public class MonteCarloCFR {

    public static HoldEmNodeMap mccfr_Pruning(int iterations, HoldEmNodeMap nodeMap) {
        for (int i=0;i<iterations;i++) {
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
        for(int p = 0; p< Constants.NUM_PLAYERS; p++) {
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
        double temp = (double)iteration / ApplicationConfiguration.DISCOUNT_INTERVAL;
        return temp / (temp + 1);
    }

    private static double traverseMCCFR_NoPruning(HoldEmNodeMap nodeMap, HoldEmGameTree state, int traversingPlayerId) {
        if (state.isTerminalForPlayer()) {
            return state.getPayoffForPlayer(traversingPlayerId);
        } else if (state.isCurrentPlayer(traversingPlayerId)) {
            Node node = nodeMap.getNodeForCurrentPlayer(state);
            double[] strategy = node.calculateStrategy();
            double expectedValue = 0;
            int numActions = state.numActions();
            double[] valueOfTakingAction = new double[numActions];
            for (int a = 0; a< numActions;a++) {
                valueOfTakingAction[a] = traverseMCCFR_NoPruning(nodeMap, state.takeAction(a), traversingPlayerId);
                expectedValue += strategy[a] * valueOfTakingAction[a];
            }
            for (int a=0;a<numActions;a++) {
                node.addRegretForAction(a, (int)(valueOfTakingAction[a] - expectedValue));
            }
            nodeMap.updateForCurrentPlayer(state, node);
            return expectedValue;
        } else {
            Node node = nodeMap.getNodeForCurrentPlayer(state);
            double[] strategy = node.calculateStrategy();
            double accumulatedActionProbability = 0;
            double randomActionProbability = ThreadLocalRandom.current().nextDouble();
            for (int a=0;a<strategy.length;a++) {
                accumulatedActionProbability += strategy[a];
                if (randomActionProbability < accumulatedActionProbability) {
                    return traverseMCCFR_NoPruning(nodeMap, state.takeAction(a), traversingPlayerId);
                }
            }
        }
        throw new IllegalStateException();
    }

    private static double traverseMCCFR_WithPruning(HoldEmNodeMap nodeMap, HoldEmGameTree state, int traversingPlayerId) {
        if (state.isTerminalForPlayer()) {
            return state.getPayoffForPlayer(traversingPlayerId);
        } else if (state.isCurrentPlayer(traversingPlayerId)) {
            Node node = nodeMap.getNodeForCurrentPlayer(state);
            double[] strategy = node.calculateStrategy();
            double expectedValue = 0;
            int numActions = state.numActions();
            boolean[] explored = new boolean[numActions];
            double[] valueOfTakingAction = new double[numActions];
            for (int a = 0; a< numActions;a++) {
                if (node.regretForActionisAboveLimit(a, ApplicationConfiguration.MINIMUM_REGRET)) {
                    valueOfTakingAction[a] = traverseMCCFR_WithPruning(nodeMap, state.takeAction(a), traversingPlayerId);
                    explored[a] = true;
                    expectedValue += strategy[a] * valueOfTakingAction[a];
                } else {
                    explored[a] = false;
                }
            }
            for (int a=0;a<numActions;a++) {
                if (explored[a]) {
                    node.addRegretForAction(a, (int)(valueOfTakingAction[a] - expectedValue));
                }
            }
            nodeMap.updateForCurrentPlayer(state, node);
            return expectedValue;
        } else {
            Node node = nodeMap.getNodeForCurrentPlayer(state);
            double[] strategy = node.calculateStrategy();
            int action = chooseRandomAction(strategy);
            return traverseMCCFR_WithPruning(nodeMap, state.takeAction(action), traversingPlayerId);
        }
    }

    private static int chooseRandomAction(double[] strategy) {
        double accumulatedActionProbability = 0;
        double randomActionProbability = ThreadLocalRandom.current().nextDouble();
        for (int a=0;a<strategy.length;a++) {
            accumulatedActionProbability += strategy[a];
            if (randomActionProbability < accumulatedActionProbability) {
                return a;
            }
        }
        throw new IllegalStateException();
    }
    private static void updateStrategy(HoldEmNodeMap nodeMap, HoldEmGameTree state, int traversingPlayer) {
        if (state.isTerminalForPlayer() || !state.shouldUpdateRegrets()) {
            return;
        } else if (state.isCurrentPlayer(traversingPlayer)) {
            Node node = nodeMap.getNodeForCurrentPlayer(state);
            double[] strategy = node.calculateStrategy();
            int chosenAction = chooseRandomAction(strategy);
            node.visitAction(chosenAction);
            updateStrategy(nodeMap, state.takeAction(chosenAction), traversingPlayer);
        } else {
            int actions = state.numActions();
            for (int a=0;a<actions;a++) {
                updateStrategy(nodeMap, state.takeAction(a), traversingPlayer);
            }
        }
    }
}
