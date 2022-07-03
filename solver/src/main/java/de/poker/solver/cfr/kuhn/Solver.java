package de.poker.solver.cfr.kuhn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Solver {
    public static final int NUM_CARDS = 3;
    public static final int NUM_ACTIONS = 2;
    public static final int NUM_PLAYERS = 2;

    Map<String, Node> nodeMap = new HashMap<>();
    double expectedGameValue = 0;
    int[] deck = new int[NUM_CARDS];
    Random random;

    public static void main(String[] args) {
        Solver solver = new Solver();
        solver.random = new Random(123L);
        solver.train(1000000);
        System.out.println(solver);
    }

    public void train(int iterations) {
        double[] expectedGameValue = new double[NUM_PLAYERS];
        for (int i=0;i<iterations;i++) {
            shuffleDeck();
            double[] cfr = cfr("", new double[]{1.0, 1.0});
            for (int p=0;p<NUM_PLAYERS;p++) {
                expectedGameValue[p] += cfr[p];
            }
            nodeMap.values().forEach(Node::updateStrategy);
        }
        for (int p=0;p<NUM_PLAYERS;p++) {
            expectedGameValue[p] /= iterations;
            System.out.println(expectedGameValue[p]);
        }
    }

    private void shuffleDeck() {
        for (int j=0;j<NUM_CARDS;j++) {
            deck[j] = -1;
        }
        for (int j=0;j<NUM_CARDS;j++) {
            int nextCard;
            do {
                nextCard = random.nextInt(NUM_CARDS);
            } while (isCardAlreadyInDeck(nextCard));
            deck[j] = nextCard;
        }
    }

    public double[] cfr(String history, double[] probability) {
        int currentPlayer = history.length() % 2;

        if (isTerminal(history)) {
            return getReward(history);
        }

        int playerCard = deck[currentPlayer];

        Node node = getNode(playerCard, history);
        double[] strategy = node.strategy;
        double[][] actionUtility = new double[node.numActions][];

        for (int i=0;i<node.numActions;i++) {
            String nextHistory = history + node.actionDictionary[i];
            double nextProbability[] = Arrays.copyOf(probability, probability.length);
            nextProbability[currentPlayer] *= strategy[i];
            actionUtility[i] = cfr(nextHistory, nextProbability);
        }

        double[] utilitySum = new double[NUM_PLAYERS];
        for (int p=0;p<NUM_PLAYERS;p++) {
            for (int i=0;i<node.numActions;i++) {
                utilitySum[p] += actionUtility[i][p] * strategy[i];
            }
        }
        double[] regrets = new double[node.numActions];
        for (int i=0;i<node.numActions;i++) {
            regrets[i] = actionUtility[i][currentPlayer] - utilitySum[currentPlayer];
        }

        node.reachProbability += probability[currentPlayer];

        for (int i=0;i<node.numActions;i++) {
            //node.regretSum[i] += regrets[i];
            // TODO check if this makes a difference
            int p = currentPlayer == 0 ? 1 : 0;
            node.regretSum[i] += probability[p] * regrets[i];
        }
        return utilitySum;
    }

    private boolean isTerminal(String history) {
        return history.endsWith("pp") || history.endsWith("bb") || history.endsWith("bp");
    }

    public double[] getReward(String history) {
        // pp, pbp, pbb, bp, bb
        if (history.endsWith("p")) {
            if (history.endsWith("pp")) {
                double p1Reward = deck[0] > deck[1] ? 1 : -1;
                return new double[]{p1Reward, -p1Reward};
            } else {
                double[] reward = new double[NUM_PLAYERS];
                reward[history.length() % NUM_PLAYERS] = 1;
                reward[(history.length() + 1) % NUM_PLAYERS] = -1;
                return reward;
            }
        } else if (history.endsWith("bb")) {
            double p1Reward = deck[0] > deck[1] ? 2 : -2;
            return new double[]{p1Reward, -p1Reward};
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
