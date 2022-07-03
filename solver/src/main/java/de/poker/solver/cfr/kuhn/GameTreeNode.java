package de.poker.solver.cfr.kuhn;

import java.util.Random;

import static de.poker.solver.cfr.kuhn.Config.NUM_PLAYERS;

public class GameTreeNode {
    String history;
    int currentPlayer;
    int[] deck;

    GameTreeNode() {
        history = "";
        currentPlayer = 0;
    }

    public static GameTreeNode initialize(Random random) {
        int card1 = random.nextInt(3);
        int card2;
        do {
            card2 = random.nextInt(3);
        } while (card2 == card1);
        GameTreeNode gameTreeNode = new GameTreeNode();
        gameTreeNode.deck = new int[]{card1, card2};
        return gameTreeNode;
    }

    public int currentPlayer() {
        return currentPlayer;
    }

    public double[] getRewards() {
        if (history.endsWith("p")) {
            if (history.endsWith("pp")) {
                double p1Reward = deck[0] > deck[1] ? 1 : -1;
                return new double[]{p1Reward, -p1Reward};
            } else {
                double reward[] = new double[NUM_PLAYERS];
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

    public boolean isTerminal() {
        return history.endsWith("pp") || history.endsWith("bb") || history.endsWith("bp");
    }

    public String infoSet() {
        return deck[currentPlayer] + history;
    }

    public int legalActions() {
        return 2;
    }

    public Node toNode() {
        return new Node(deck[currentPlayer] + history, legalActions());
    }

    public GameTreeNode takeAction(int actionId) {
        GameTreeNode gameTreeNode = new GameTreeNode();
        gameTreeNode.deck = deck;
        gameTreeNode.currentPlayer = currentPlayer == 0 ? 1 : 0;
        if (actionId == 0) {
            gameTreeNode.history = history + "p";
        } else {
            gameTreeNode.history = history + "b";
        }
        return gameTreeNode;
    }
}
