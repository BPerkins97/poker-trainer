package de.poker.solver.pluribus;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class MonteCarloCFR {

    public static Map<InfoSet, Node> mccfr_Pruning(GameTree root, Configuration config, int iterations, Map<InfoSet, Node> nodeMap) {
        for (int p=0;p<root.numPlayers();p++) {
            for(Map.Entry<InfoSet, Node> entry : nodeMap.entrySet()) {
                if (entry.getKey().isCurrentPlayer(p)) {
                    entry.getValue().resetRegrets();
                    if (entry.getKey().shouldCalculateBluePrintStrategy()) {
                        entry.getValue().resetAverageStrategy();
                    }
                }
            }
        }

        for (int i=0;i<iterations;i++) {
            for(int p=0;p<root.numPlayers();p++) {
                if (i % config.strategyInterval() == 0) {
                    updateStrategy(nodeMap, root, p);
                }
                if (i > config.pruningThreshold()) {
                    double randomNumber = ThreadLocalRandom.current().nextDouble();
                    if (randomNumber < 0.05) {
                        traverseMCCFR_NoPruning(nodeMap, root, p);
                    } else {
                        traverseMCCFR_WithPruning(config, nodeMap, root, p);
                    }
                } else {
                    traverseMCCFR_NoPruning(nodeMap, root, p);
                }
            }
            if (i < config.linearCFRThreshold() && i % config.discountInterval() == 0) {
                double discountValue = calculateDiscountValue(i, config);
                for(Map.Entry<InfoSet, Node> entry : nodeMap.entrySet()) {
                    entry.getValue().discount(discountValue);
                }
            }
        }

        return nodeMap;
    }

    private static double calculateDiscountValue(int iteration, Configuration config) {
        double temp = (double)iteration / config.discountInterval();
        return temp / (temp + 1);
    }

    private static double traverseMCCFR_NoPruning(Map<InfoSet, Node> nodeMap, GameTree state, int traversingPlayerId) {
        if (state.isTerminalForPlayer(traversingPlayerId)) {
            return state.getPayoff(traversingPlayerId);
        } else if (state.isCurrentPlayer(traversingPlayerId)) {
            InfoSet infoSet = state.asInfoSet();
            Node node = nodeMap.get(infoSet);
            double[] strategy = node.calculateStrategy();
            double expectedValue = 0;
            int numActions = state.actions();
            double[] valueOfTakingAction = new double[numActions];
            for (int a = 0; a< numActions;a++) {
                valueOfTakingAction[a] = traverseMCCFR_NoPruning(nodeMap, state.takeAction(a), traversingPlayerId);
                expectedValue += strategy[a] * valueOfTakingAction[a];
            }
            for (int a=0;a<numActions;a++) {
                node.addRegretForAction(a, valueOfTakingAction[a] - expectedValue);
            }
            nodeMap.put(infoSet, node);
            return expectedValue;
        } else {
            InfoSet infoSet = state.asInfoSet();
            Node node = nodeMap.get(infoSet);
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

    public static double traverseMCCFR_WithPruning(Configuration config, Map<InfoSet, Node> regrets, GameTree state, int traversingPlayerId) {
        if (state.isTerminalForPlayer(traversingPlayerId)) {
            return state.getPayoff(traversingPlayerId);
        } else if (state.isCurrentPlayer(traversingPlayerId)) {
            InfoSet infoSet = state.asInfoSet();
            Node node = regrets.get(infoSet);
            double[] strategy = node.calculateStrategy();
            double expectedValue = 0;
            int numActions = state.actions();
            boolean[] explored = new boolean[numActions];
            double[] valueOfTakingAction = new double[numActions];
            for (int a = 0; a< numActions;a++) {
                if (node.regretForActionisAboveLimit(a, config.minimumRegret())) {
                    valueOfTakingAction[a] = traverseMCCFR_WithPruning(config, regrets, state.takeAction(a), traversingPlayerId);
                    explored[a] = true;
                    expectedValue += strategy[a] * valueOfTakingAction[a];
                } else {
                    explored[a] = false;
                }
            }
            for (int a=0;a<numActions;a++) {
                if (explored[a]) {
                    node.addRegretForAction(a, valueOfTakingAction[a] - expectedValue);
                }
            }
            regrets.put(infoSet, node);
            return expectedValue;
        } else {
            InfoSet infoSet = state.asInfoSet();
            Node node = regrets.get(infoSet);
            double[] strategy = node.calculateStrategy();
            int action = chooseRandomAction(strategy);
            return traverseMCCFR_WithPruning(config, regrets, state.takeAction(action), traversingPlayerId);
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
    private static void updateStrategy(Map<InfoSet, Node> nodeMap, GameTree state, int traversingPlayer) {
        if (state.isTerminalForPlayer(traversingPlayer) || !state.shouldUpdateRegrets()) {
            return;
        } else if (state.isCurrentPlayer(traversingPlayer)) {
            InfoSet infoSet = state.asInfoSet();
            Node node = nodeMap.get(infoSet);
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
