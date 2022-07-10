package de.poker.solver.kuhn;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

// TODO pruning of very bad actions
public class Solver {
    public static final int NUM_CARDS = 3;
    public static final int NUM_ACTIONS = 2;
    private static final int[][] PERMUTATIONS = new int[6][];

    static {
        PERMUTATIONS[0] = new int[]{0,1,2};
        PERMUTATIONS[1] = new int[]{0,2,1};
        PERMUTATIONS[2] = new int[]{1,2,0};
        PERMUTATIONS[3] = new int[]{1,0,2};
        PERMUTATIONS[4] = new int[]{2,1,0};
        PERMUTATIONS[5] = new int[]{2,0,1};
    }

    Map<String, Node> nodeMap = new HashMap<>();
    double expectedGameValue = 0;
    int[] deck = new int[NUM_CARDS];
    private Random random;

    public static void main(String[] args) {
        Solver solver = new Solver();
        solver.train(ThreadLocalRandom.current(), 10000, 0.01, true);
        solver.nodeMap
                .values()
                .stream().sorted(Comparator.comparing(v -> v.key.length()))
                .forEach(System.out::println);
    }

    private double calculateBestResponse(
            Map<String, Node> infoSetMap,
            Map<String, double[]> bestResponseMap,
            int bestResponsePlayer,
            int[] cards,
            String history,
            int activePlayer,
            double probability) {
        if (isTerminal(history)) {
            return -getReward(history, cards);
        }
        String key = cards[activePlayer] + history;
        int nextPlayer = (activePlayer + 1) % 2; // TODO den Algorithmus übernehmen für andere use cases
        if (activePlayer == bestResponsePlayer) {
            if (!bestResponseMap.containsKey(key)) {
                bestResponseMap.put(key, new double[]{0,0});
            }
            double[] values = bestResponseMap.get(key);
            double bestResponseValue = Integer.MIN_VALUE;
            for(int i=0;i<NUM_ACTIONS;i++) {
                double value = calculateBestResponse(infoSetMap, bestResponseMap, bestResponsePlayer, cards, history + actionToString(i), nextPlayer, probability);
                values[i] += probability * value;
                bestResponseValue = Math.max(value, bestResponseValue);
            }
            return -bestResponseValue;
        } else {
            if (!infoSetMap.containsKey(key)) {
                infoSetMap.put(key, new Node(key, new String[]{"p", "b"}));
            }
            Node node = infoSetMap.get(key);
            double[] strategy = node.getAverageStrategy();
            double bestResponseValue = 0;
            for (int i=0;i<NUM_ACTIONS;i++) {
                double value = calculateBestResponse(nodeMap, bestResponseMap, bestResponsePlayer, cards, history + actionToString(i), nextPlayer, probability * strategy[i]);
                bestResponseValue += value * strategy[i];
            }
            return -bestResponseValue;
        }
    }

    private double calculateExploitability(Map<String, Node> infoSetMap) {
        HashMap<String, double[]> bestResponseStrategy = new HashMap<>();
        for (int[] permutation : PERMUTATIONS) {
            calculateBestResponse(infoSetMap, bestResponseStrategy, 0, permutation, "", 0, 1.0);
            calculateBestResponse(infoSetMap, bestResponseStrategy, 1, permutation, "", 0, 1.0);
        }
        for (String key : bestResponseStrategy.keySet()) {
            double[] values = bestResponseStrategy.get(key);
            int maxIndex = 0;
            for (int i=1;i<values.length;i++) {
                if (values[i] > values[maxIndex]) {
                    maxIndex = i;
                }
            }
            for (int i=0;i<values.length;i++) {
                values[i] = values[maxIndex] == values[i] ? 1 : 0;
            }
        }
        Map<String, double[]> cfrStrategy = new HashMap<>();
        infoSetMap.forEach((key, value) -> cfrStrategy.put(key, value.getAverageStrategy()));

        double exploitability = 0;
        for (int[] permutation : PERMUTATIONS) {
            double ev1 = calculateExpectedValue(cfrStrategy, bestResponseStrategy, permutation, "", 0);
            double ev2 = calculateExpectedValue(bestResponseStrategy, cfrStrategy, permutation, "", 0);
            exploitability += ev1 - ev2;
        }
        return exploitability / PERMUTATIONS.length;
    }

    private double calculateExpectedValue(Map<String, double[]> player1Strategy, Map<String, double[]> player2Strategy, int[] cards, String history, int activePlayer) {
        if (isTerminal(history)) {
            return -getReward(history, cards);
        }
        int myCard = cards[history.length() % 2];
        int nextPlayer = (activePlayer + 1) % 2;
        double[] strategy;
        if (activePlayer == 0) {
            strategy = player1Strategy.get(myCard + history);
        } else {
            strategy = player2Strategy.get(myCard + history);
        }
        double expectedValue = 0;
        for (int i=0;i<NUM_ACTIONS;i++) {
            expectedValue += -1 * strategy[i] * calculateExpectedValue(player1Strategy, player2Strategy, cards, history + actionToString(i), nextPlayer);
        }
        return expectedValue;
    }

    public double train(Random random, int iterations, double stopAtExploitability, boolean debug) {
        this.random = random;
        expectedGameValue = 0;
        double exploitability;
        int counter = 0;
        do {
            deck = PERMUTATIONS[random.nextInt(0, PERMUTATIONS.length)];
            expectedGameValue += cfr("", 1.0, 1.0, 0);
            cfr("", 1.0, 1.0, 1);
            nodeMap.values().forEach(Node::updateStrategy);
            exploitability = calculateExploitability(nodeMap);
            counter++;
        } while (counter < iterations && exploitability > stopAtExploitability);
        if (debug) {
            System.out.println("Took " + counter + " to stop at " + exploitability + " exploitability");
            expectedGameValue /= counter * PERMUTATIONS.length;
            System.out.println("Expected game Value: " + expectedGameValue);
        }
        return expectedGameValue;
    }

    public double cfr(String history, double probabilityPlayer1, double probabilityPlayer2, int iteratingPlayer) {
        int historyLength = history.length();
        int currentPlayer = historyLength % 2;
        boolean isPlayer1Turn = currentPlayer == 0;

        if (isTerminal(history)) {
            return getReward(history, iteratingPlayer);
        }

        Node node = getNode(deck[currentPlayer], history);
        double[] strategy = node.strategy;

        if (iteratingPlayer == currentPlayer) {
            double[] actionUtility = new double[node.numActions];
            double utilitySum = 0;
            for (int i=0;i<node.numActions;i++) {
                String nextHistory = history + node.actionDictionary[i];
                if (isPlayer1Turn) {
                    actionUtility[i] = cfr(nextHistory, probabilityPlayer1 * strategy[i], probabilityPlayer2, iteratingPlayer);
                } else {
                    actionUtility[i] = cfr(nextHistory, probabilityPlayer1, probabilityPlayer2 * strategy[i], iteratingPlayer);
                }
                utilitySum += actionUtility[i] * strategy[i];
            }

            double[] regrets = new double[node.numActions];
            for (int i=0;i<node.numActions;i++) {
                regrets[i] = actionUtility[i] - utilitySum;
                if (isPlayer1Turn) {
                    regrets[i] *= probabilityPlayer2;
                } else {
                    regrets[i] *= probabilityPlayer1;
                }
            }
            node.reachProbability += isPlayer1Turn ? probabilityPlayer1 : probabilityPlayer2;
            node.updateRegrets(regrets);
            return utilitySum;
        } else {
            int action = pickRandomAction(strategy);
            String nextHistory = history + node.actionDictionary[action];
            if (isPlayer1Turn) {
                return cfr(nextHistory, probabilityPlayer1 * strategy[action], probabilityPlayer2, iteratingPlayer);
            } else {
                return cfr(nextHistory, probabilityPlayer1, probabilityPlayer2 * strategy[action], iteratingPlayer);
            }
        }
    }

    private int pickRandomAction(double[] strategy) {
        double probability = random.nextDouble();
        double accumulatedActionProbability = 0;
        for (int i=0;i<strategy.length;i++) {
            accumulatedActionProbability += strategy[i];
            if (probability < accumulatedActionProbability) {
                return i;
            }
        }
        return 0;
    }

    private boolean isTerminal(String history) {
        return history.endsWith("pp") || history.endsWith("bb") || history.endsWith("bp");
    }

    public double getReward(String history, int[] cards) {
        boolean isPlayer1Turn = history.length() % 2 == 0;
        int playerCard = isPlayer1Turn ? cards[0] : cards[1];
        int opponentCard = isPlayer1Turn ? cards[1] : cards[0];
        return getReward(history, playerCard, opponentCard);
    }

    public double getReward(String history, int iteratingPlayer) {
        int playerCard = deck[iteratingPlayer];
        int opponentCard = deck[(iteratingPlayer + 1) % 2];
        if (history.endsWith("p")) {
            if (history.endsWith("pp")) {
                return playerCard > opponentCard ? 1 : -1;
            } else {
                int currentPlayer = history.length() % 2;
                return currentPlayer == iteratingPlayer ? 1 : -1;
            }
        } else if (history.endsWith("bb")) {
            return playerCard > opponentCard ? 2 : -2;
        }
        throw new IllegalStateException();
    }

    public double getReward(String history, int playerCard, int opponentCard) {
        if (history.endsWith("p")) {
            if (history.endsWith("pp")) {
                return playerCard > opponentCard ? 1 : -1;
            } else {
                return 1;
            }
        } else if (history.endsWith("bb")) {
            return playerCard > opponentCard ? 2 : -2;
        }
        throw new IllegalStateException();
    }

    public Node getNode(int card, String history) {
        String key = "" + card + history;
        if (!nodeMap.containsKey(key)) {
            String[] actionDictionary = new String[NUM_ACTIONS];
            actionDictionary[0] = actionToString(0);
            actionDictionary[1] = actionToString(1);
            Node node = new Node(key, actionDictionary);
            nodeMap.put(key, node);
        }
        return nodeMap.get(key);
    }

    public String actionToString(int action) {
        return switch (action) {
            case 0 -> "p";
            case 1 -> "b";
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        nodeMap.values().forEach(node -> sb.append(node).append("\n"));
        return sb.toString();
    }
}
