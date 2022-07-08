package de.poker.solver.cfr.kuhn;

import java.awt.event.WindowFocusListener;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Solver {
    public static final int NUM_CARDS = 3;
    public static final int NUM_ACTIONS = 2;
    public static final int NUM_PLAYERS = 2;
    private static final int[][] PERMUTATIONS = new int[6][];
    public static final int ACTION_FOLD = 0;

    static {
        PERMUTATIONS[0] = new int[]{0, 1, 2};
        PERMUTATIONS[1] = new int[]{0, 2, 1};
        PERMUTATIONS[2] = new int[]{1, 2, 0};
        PERMUTATIONS[3] = new int[]{1, 0, 2};
        PERMUTATIONS[4] = new int[]{2, 1, 0};
        PERMUTATIONS[5] = new int[]{2, 0, 1};
    }

    Map<String, Node> nodeMap = new HashMap<>();

    public static void main(String[] args) {
        Solver solver = new Solver();
        solver.train(10000, 0.001, true);
        solver.nodeMap
                .values()
                .stream().sorted(Comparator.comparing(v -> v.key.length()))
                .forEach(System.out::println);
    }

    private double[][] calculateBestResponse(
            Map<String, Node> infoSetMap,
            Map<String, double[][]> bestResponseMap,
            int bestResponsePlayer,
            String history,
            int activePlayer,
            double[][] probability) {
        if (isTerminal(history)) {
            return getReward(history);
        }
        String key = history;
        int nextPlayer = (activePlayer + 1) % 2; // TODO den Algorithmus übernehmen für andere use cases
        if (activePlayer == bestResponsePlayer) {
            if (!bestResponseMap.containsKey(key)) {
                bestResponseMap.put(key, new double[NUM_CARDS][]);
                for (int c = 0; c < NUM_CARDS; c++) {
                    bestResponseMap.get(key)[c] = new double[NUM_ACTIONS];
                }
            }
            double[][] values = bestResponseMap.get(key);
            double[][] bestResponseValue = new double[NUM_PLAYERS][NUM_CARDS];

            for (int p = 0; p < NUM_PLAYERS; p++) {
                for (int c = 0; c < NUM_CARDS; c++) {
                    bestResponseValue[p][c] = Double.MIN_VALUE;
                }
                for (int a = 0; a < NUM_ACTIONS; a++) {
                    for (int c = 0; c < NUM_CARDS; c++) {
                        double[][] value = calculateBestResponse(infoSetMap, bestResponseMap, bestResponsePlayer, history + actionToString(a), nextPlayer, probability);
                        values[c][a] += probability[p][c] * value[p][c];
                        bestResponseValue[p][c] = Math.max(value[p][c], bestResponseValue[p][c]);
                    }
                }
            }

            return bestResponseValue;
        } else {
            if (!infoSetMap.containsKey(key)) {
                infoSetMap.put(key, new Node(key, new String[]{"p", "b"}, NUM_CARDS));
            }
            Node node = infoSetMap.get(key);
            double[][] strategy = node.getAverageStrategy();
            double[][] bestResponseValue = new double[NUM_PLAYERS][NUM_CARDS];
            for (int p = 0; p < NUM_PLAYERS; p++) {
                for (int a = 0; a < NUM_ACTIONS; a++) {
                    double[][] newProbability = new double[NUM_PLAYERS][NUM_CARDS];
                    for (int c = 0; c < NUM_CARDS; c++) {
                        newProbability[p][c] = probability[p][c] * strategy[c][a];
                    }
                    for (int c = 0; c < NUM_CARDS; c++) {
                        double[][] value = calculateBestResponse(nodeMap, bestResponseMap, bestResponsePlayer, history + actionToString(a), nextPlayer, newProbability);
                        bestResponseValue[p][c] += value[p][c] * strategy[c][a];
                    }
                }
            }
            return bestResponseValue;
        }
    }

    private double calculateExploitability(Map<String, Node> infoSetMap) {
        HashMap<String, double[][]> bestResponseStrategy = new HashMap<>();
        double[][] probabilities = new double[NUM_PLAYERS][NUM_CARDS];
        Arrays.fill(probabilities[0], 1.0);
        Arrays.fill(probabilities[1], 1.0);
        calculateBestResponse(infoSetMap, bestResponseStrategy, 0, "", 0, probabilities);
        calculateBestResponse(infoSetMap, bestResponseStrategy, 1, "", 0, probabilities);
        for (String key : bestResponseStrategy.keySet()) {
            double[][] values = bestResponseStrategy.get(key);
            int[] maxIndex = new int[NUM_CARDS];
            for (int c = 0; c < NUM_CARDS; c++) {
                for (int a = 1; a < NUM_ACTIONS; a++) {
                    if (values[c][a] > values[c][maxIndex[c]]) {
                        maxIndex[c] = a;
                    }
                }
                for (int a = 0; a < NUM_ACTIONS; a++) {
                    values[c][a] = values[c][maxIndex[c]] == values[c][a] ? 1 : 0;
                }
            }
        }
        Map<String, double[][]> cfrStrategy = new HashMap<>();
        infoSetMap.forEach((key, value) -> {
            cfrStrategy.put(key, value.getAverageStrategy());
        });

        double[][] ev1 = calculateExpectedValue(cfrStrategy, bestResponseStrategy, "", 0);
        double[][] ev2 = calculateExpectedValue(bestResponseStrategy, cfrStrategy, "", 0);

        double exploitability = 0;
        for (int p=0;p<NUM_PLAYERS;p++) {
            for (int c = 0; c < NUM_CARDS; c++) {
                exploitability += ev1[p][c] - ev2[p][c];
            }
        }
        return exploitability / PERMUTATIONS.length / NUM_PLAYERS;
    }

    private double[][] calculateExpectedValue(Map<String, double[][]> player1Strategy, Map<String, double[][]> player2Strategy, String history, int activePlayer) {
        if (isTerminal(history)) {
            return getReward(history);
        }
        int nextPlayer = (activePlayer + 1) % 2;
        double[][] strategy;
        if (activePlayer == 0) {
            strategy = player1Strategy.get(history);
        } else {
            strategy = player2Strategy.get(history);
        }
        double[][] expectedValue = new double[NUM_PLAYERS][NUM_CARDS];
        for (int p = 0; p < NUM_PLAYERS; p++) {
            for (int c = 0; c < NUM_CARDS; c++) {
                for (int a = 0; a < NUM_ACTIONS; a++) {
                    double[][] expectedValues = calculateExpectedValue(player1Strategy, player2Strategy, history + actionToString(a), nextPlayer);
                    expectedValue[p][c] += strategy[c][a] * expectedValues[p][c];
                }
            }
        }
        return expectedValue;
    }

    public double[][] train(int iterations, double stopAtExploitability, boolean debug) {
        double[][] expectedGameValue = new double[NUM_PLAYERS][NUM_CARDS];
        double exploitability;
        int counter = 0;
        do {
            double[][] probabilities = new double[NUM_PLAYERS][NUM_CARDS];
            Arrays.fill(probabilities[0], 1.0);
            Arrays.fill(probabilities[1], 1.0);
            double[][] cfr = cfr("", probabilities);
            for (int p=0;p<NUM_PLAYERS;p++) {
                for (int c = 0; c < NUM_CARDS; c++) {
                    expectedGameValue[p][c] += cfr[p][c];
                }
            }
            nodeMap.values().forEach(Node::updateStrategy);
            exploitability = calculateExploitability(nodeMap);
            counter++;
        } while (counter < iterations && exploitability > stopAtExploitability);
        if (debug) {
            System.out.println("Took " + counter + " to stop at " + exploitability + " exploitability");
            for (int p=0;p<NUM_PLAYERS;p++) {
                for (int c = 0; c < NUM_CARDS; c++) {
                    expectedGameValue[p][c] /= counter * PERMUTATIONS.length;
                    System.out.println("EV\t" + c + "\t" + expectedGameValue[p][c]);
                }
            }
        }
        return expectedGameValue;
    }

    public double[][] cfr(String history, double[][] probabilities) {
        int historyLength = history.length();
        int currentPlayer = historyLength % NUM_PLAYERS;
        boolean isPlayer1Turn = currentPlayer == 0;

        if (isTerminal(history)) {
            return getReward(history);
        }

        Node node = getNode(history);
        double[][] strategy = node.strategy;
        double[][][] actionUtility = new double[NUM_PLAYERS][node.numCards][node.numActions];
        for (int a = 0; a < node.numActions; a++) {
            String nextHistory = history + node.actionDictionary[a];
            double[][] cfr;
            double[][] newProbabilities = new double[NUM_PLAYERS][NUM_CARDS];
            newProbabilities[(currentPlayer+1) % NUM_PLAYERS] = probabilities[(currentPlayer+1) % NUM_PLAYERS];
            for (int c=0;c<NUM_CARDS;c++) {
                newProbabilities[currentPlayer][c] = strategy[c][a] * probabilities[currentPlayer][c];
            }
            if (isPlayer1Turn) {
                cfr = cfr(nextHistory, probabilities);
            } else {
                cfr = cfr(nextHistory, probabilities);
            }
            for (int p = 0; p < NUM_PLAYERS; p++) {
                for (int c = 0; c < node.numCards; c++) {
                    actionUtility[p][c][a] += cfr[p][a];
                }
            }
        }


        double[][] regrets = new double[node.numCards][];
        double[][] utilitySum = new double[NUM_PLAYERS][NUM_CARDS];
        for (int c = 0; c < node.numCards; c++) {
            for (int a = 0; a < node.numActions; a++) {
                for (int p = 0; p < NUM_PLAYERS; p++) {
                    utilitySum[p][c] += actionUtility[p][c][a] * strategy[c][a];
                }
            }
            regrets[c] = new double[node.numActions];
            for (int a = 0; a < node.numActions; a++) {
                regrets[c][a] = actionUtility[currentPlayer][c][a] - utilitySum[currentPlayer][c];
            }
        }

        node.addProbability(probabilities[currentPlayer]);
        node.addRegrets(regrets, probabilities[(currentPlayer+1)%NUM_PLAYERS]);
        return utilitySum;
    }

    private double[] calculateNewProbabilities(double[] probability, Node node, int action) {
        double[] newProbabilities = new double[node.numCards];
        for (int c = 0; c < node.numCards; c++) {
            newProbabilities[c] = probability[c] * node.strategy[c][action];
        }
        return newProbabilities;
    }

    private boolean isTerminal(String history) {
        return history.endsWith("pp") || history.endsWith("bb") || history.endsWith("bp");
    }

    public double[][] getReward(String history) {
        if (history.endsWith("p")) {
            if (history.endsWith("pp")) {
                double[][] winnings = new double[NUM_PLAYERS][];
                winnings[0] = calculateWinnings(1, -1);
                winnings[1] = winnings[0];
                return winnings;
            } else {
                double[][] returnValue = new double[NUM_PLAYERS][NUM_CARDS];
                Arrays.fill(returnValue[0], 1);
                Arrays.fill(returnValue[1], -1);
                return returnValue;
            }
        } else if (history.endsWith("bb")) {
            double[][] winnings = new double[NUM_PLAYERS][];
            winnings[0] = calculateWinnings(2, -2);
            winnings[1] = winnings[0];
            return winnings;
        }
        throw new IllegalStateException();
    }

    private double[] calculateWinnings(int winValue, int loseValue) {
        double[] returnValue = new double[NUM_CARDS];
        for (int playerCard = 0; playerCard < NUM_CARDS; playerCard++) {
            for (int opponentCard = 0; opponentCard < NUM_CARDS; opponentCard++) {
                if (playerCard != opponentCard) {
                    returnValue[playerCard] += playerCard > opponentCard ? winValue : loseValue;
                }
            }
        }
        return returnValue;
    }

    public Node getNode(String history) {
        String key = history;
        if (!nodeMap.containsKey(key)) {
            String[] actionDictionary = new String[NUM_ACTIONS];
            actionDictionary[0] = actionToString(0);
            actionDictionary[1] = actionToString(1);
            Node node = new Node(key, actionDictionary, NUM_CARDS);
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
