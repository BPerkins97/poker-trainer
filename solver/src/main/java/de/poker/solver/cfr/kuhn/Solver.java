package de.poker.solver.cfr.kuhn;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Solver {
    public static final int NUM_CARDS = 3;
    public static final int NUM_ACTIONS = 2;

    Map<String, Node> nodeMap = new HashMap<>();
    double expectedGameValue = 0;
    int currentPlayer = 0;
    int[] deck = new int[NUM_CARDS];

    public static void main(String[] args) {
        Solver solver = new Solver();
        solver.train(50000);
        solver.nodeMap
                .values()
                .stream().sorted(Comparator.comparing(v -> v.key.length()))
                .forEach(System.out::println);
    }



    public void train(int iterations) {
        expectedGameValue = 0;
        for (int i=0;i<iterations;i++) {
            shuffleDeck();
            expectedGameValue += cfr("", 1.0, 1.0);
            nodeMap.values().forEach(Node::updateStrategy);
        }
        expectedGameValue /= iterations;
        System.out.println(expectedGameValue);
    }

    private void shuffleDeck() {
        for (int j=0;j<NUM_CARDS;j++) {
            deck[j] = -1;
        }
        for (int j=0;j<NUM_CARDS;j++) {
            int nextCard;
            do {
                nextCard = ThreadLocalRandom.current().nextInt(NUM_CARDS);
            } while (isCardAlreadyInDeck(nextCard));
            deck[j] = nextCard;
        }
    }

    public double cfr(String history, double probabilityPlayer1, double probabilityPlayer2) {
        int historyLength = history.length();
        boolean isPlayer1Turn = historyLength % 2 == 0;
        int playerCard = isPlayer1Turn ? deck[0] : deck[1];

        if (isTerminal(history)) {
            int opponentCard = isPlayer1Turn ? deck[1] : deck[0];
            return getReward(history, playerCard, opponentCard);
        }

        Node node = getNode(playerCard, history);
        double[] strategy = node.strategy;
        double[] actionUtility = new double[node.numActions];

        for (int i=0;i<node.numActions;i++) {
            String nextHistory = history + node.actionDictionary[i];
            if (isPlayer1Turn) {
                actionUtility[i] = -1.0 * cfr(nextHistory, probabilityPlayer1 * strategy[i], probabilityPlayer2);
            } else {
                actionUtility[i] = -1.0 * cfr(nextHistory, probabilityPlayer1, probabilityPlayer2 * strategy[i]);
            }
        }

        double utilitySum = 0;
        for (int i=0;i<node.numActions;i++) {
            utilitySum += actionUtility[i] * strategy[i];
        }
        double[] regrets = new double[node.numActions];
        for (int i=0;i<node.numActions;i++) {
            regrets[i] = actionUtility[i] - utilitySum;
        }
        if (isPlayer1Turn) {
            node.reachProbability += probabilityPlayer1;
            for (int i=0;i<node.numActions;i++) {
                node.regretSum[i] += probabilityPlayer2 * regrets[i];
            }
        } else {
            node.reachProbability += probabilityPlayer2;
            for (int i=0;i<node.numActions;i++) {
                node.regretSum[i] += probabilityPlayer1 * regrets[i];
            }
        }
        return utilitySum;
    }

    private boolean isTerminal(String history) {
        return history.endsWith("pp") || history.endsWith("bb") || history.endsWith("bp");
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
        String key = "" + card + " " + history;
        if (!nodeMap.containsKey(key)) {
            String[] actionDictionary = new String[NUM_ACTIONS];
            actionDictionary[0] = "p";
            actionDictionary[1] = "b";
            Node node = new Node(key, actionDictionary);
            nodeMap.put(key, node);
        }
        return nodeMap.get(key);
    }

    private boolean isCardAlreadyInDeck(int nextCard) {
        return Arrays.stream(deck).anyMatch(c -> c == nextCard);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        nodeMap.values().forEach(node -> sb.append(node).append("\n"));
        return sb.toString();
    }
}
