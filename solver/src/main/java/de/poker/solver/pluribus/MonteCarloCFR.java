package de.poker.solver.pluribus;

import java.util.concurrent.ThreadLocalRandom;

// As implemented in http://www.cs.cmu.edu/~noamb/papers/19-Science-Superhuman_Supp.pdf
public class MonteCarloCFR {

    public static NodeMap mccfr_Pruning(Configuration config, int iterations, NodeMap nodeMap) {
        // TODO ich muss noch rausfinden, wofür das gut sein soll, keine Ahnung, warum setzen wir unseren Fortschritt zurück?
//        for (int p=0;p<config.numPlayers();p++) {
//            nodeMap.forEach((key, value) -> {
//                if (key.isCurrentPlayer(p)) {
//                    entry.getValue().resetRegrets();
//                    if (key.shouldCalculateBluePrintStrategy()) {
//                        entry.getValue().resetAverageStrategy();
//                    }
//                }
//            });
//        }

        for (int i=0;i<iterations;i++) {
            GameTree rootNode = config.randomRootNode();
            double randomNumber = ThreadLocalRandom.current().nextDouble();
            for(int p=0;p<config.numPlayers();p++) {
                if (i > config.pruningThreshold()) {
                    if (randomNumber < 0.05) {
                        traverseMCCFR_NoPruning(config, nodeMap, rootNode, p);
                    } else {
                        traverseMCCFR_WithPruning(config, nodeMap, rootNode, p);
                    }
                } else {
                    traverseMCCFR_NoPruning(config, nodeMap, rootNode, p);
                }
                if (i % config.strategyInterval() == 0) {
                    updateStrategy(nodeMap, rootNode, p);
                }
            }
            if (i < config.linearCFRThreshold() && i % config.discountInterval() == 0) {
                double discountValue = calculateDiscountValue(i, config);
                nodeMap.discount(discountValue);
            }
        }

        return nodeMap;
    }

    private static double calculateDiscountValue(int iteration, Configuration config) {
        double temp = (double)iteration / config.discountInterval();
        return temp / (temp + 1);
    }

    private static double traverseMCCFR_NoPruning(Configuration config, NodeMap nodeMap, GameTree state, int traversingPlayerId) {
        if (state.isTerminalForPlayer(traversingPlayerId)) {
            return state.getPayoffForPlayer(traversingPlayerId);
        } else if (state.isCurrentPlayer(traversingPlayerId)) {
            Node node = nodeMap.getNodeForCurrentPlayer(state);
            double[] strategy = node.calculateStrategy();
            double expectedValue = 0;
            int numActions = state.actions();
            double[] valueOfTakingAction = new double[numActions];
            for (int a = 0; a< numActions;a++) {
                valueOfTakingAction[a] = traverseMCCFR_NoPruning(config, nodeMap, state.takeAction(a), traversingPlayerId);
                expectedValue += strategy[a] * valueOfTakingAction[a];
            }
            for (int a=0;a<numActions;a++) {
                node.addRegretForAction(a, (int)(valueOfTakingAction[a] - expectedValue), config);
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
                    return traverseMCCFR_NoPruning(config, nodeMap, state.takeAction(a), traversingPlayerId);
                }
            }
        }
        throw new IllegalStateException();
    }

    private static double traverseMCCFR_WithPruning(Configuration config, NodeMap nodeMap, GameTree state, int traversingPlayerId) {
        if (state.isTerminalForPlayer(traversingPlayerId)) {
            return state.getPayoffForPlayer(traversingPlayerId);
        } else if (state.isCurrentPlayer(traversingPlayerId)) {
            Node node = nodeMap.getNodeForCurrentPlayer(state);
            double[] strategy = node.calculateStrategy();
            double expectedValue = 0;
            int numActions = state.actions();
            boolean[] explored = new boolean[numActions];
            double[] valueOfTakingAction = new double[numActions];
            for (int a = 0; a< numActions;a++) {
                if (node.regretForActionisAboveLimit(a, config.minimumRegret())) {
                    valueOfTakingAction[a] = traverseMCCFR_WithPruning(config, nodeMap, state.takeAction(a), traversingPlayerId);
                    explored[a] = true;
                    expectedValue += strategy[a] * valueOfTakingAction[a];
                } else {
                    explored[a] = false;
                }
            }
            for (int a=0;a<numActions;a++) {
                if (explored[a]) {
                    node.addRegretForAction(a, (int)(valueOfTakingAction[a] - expectedValue), config);
                }
            }
            nodeMap.updateForCurrentPlayer(state, node);
            return expectedValue;
        } else {
            Node node = nodeMap.getNodeForCurrentPlayer(state);
            double[] strategy = node.calculateStrategy();
            int action = chooseRandomAction(strategy);
            return traverseMCCFR_WithPruning(config, nodeMap, state.takeAction(action), traversingPlayerId);
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
    private static void updateStrategy(NodeMap nodeMap, GameTree state, int traversingPlayer) {
        if (state.isTerminalForPlayer(traversingPlayer) || !state.shouldUpdateRegrets()) {
            return;
        } else if (state.isCurrentPlayer(traversingPlayer)) {
            Node node = nodeMap.getNodeForCurrentPlayer(state);
            double[] strategy = node.calculateStrategy();
            int chosenAction = chooseRandomAction(strategy);
            node.visitAction(chosenAction);
            updateStrategy(nodeMap, state.takeAction(chosenAction), traversingPlayer);
        } else {
            int actions = state.actions();
            for (int a=0;a<actions;a++) {
                updateStrategy(nodeMap, state.takeAction(a), traversingPlayer);
            }
        }
    }
}
