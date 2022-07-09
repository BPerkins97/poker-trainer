package de.poker.solver.game;

import de.poker.solver.cfr.Node;
import de.poker.solver.utility.CardInfoSetBuilder;
import de.poker.solver.utility.KeyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// TODO if all players still in game are all in skip straight to showdown
public class GameTreeNode {
    private static final int POSITION_SMALL_BLIND = 0;
    private static final int POSITION_BIG_BLIND = 1;
    private static final int ACTION_FOLD = 0;
    private static final int ACTION_CALL = 1;
    private static final int ACTION_ALL_IN = 2;

    private static final int ROUND_PRE_FLOP = 0;
    private static final int ROUND_POST_FLOP = 1;
    private static final int ROUND_TURN = 2;
    private static final int ROUND_RIVER = 3;

    public static final int NUM_PLAYERS = 2;
    private static final int[][] BETTING_ORDER_PER_ROUND = new int[4][NUM_PLAYERS];

    private static final int NUM_BETTINGS_ROUNDS = 4;
    private static final int FLOP_CARD1;
    private static final int FLOP_CARD2;
    private static final int FLOP_CARD3;
    private static final int TURN_CARD;
    private static final int RIVER_CARD;

    static {
        for (int i=0;i<NUM_PLAYERS;i++) {
            BETTING_ORDER_PER_ROUND[0][i] = (i + 2) % NUM_PLAYERS;
            BETTING_ORDER_PER_ROUND[1][i] = i;
            BETTING_ORDER_PER_ROUND[2][i] = i;
            BETTING_ORDER_PER_ROUND[3][i] = i;
        }
        FLOP_CARD1 = NUM_PLAYERS * 2;
        FLOP_CARD2 = NUM_PLAYERS * 2 + 1;
        FLOP_CARD3 = NUM_PLAYERS * 2 + 2;
        TURN_CARD = NUM_PLAYERS * 2 + 3;
        RIVER_CARD = NUM_PLAYERS * 2 + 4;
    }

    String history;
    String[][] cardInfoSets;
    int currentPlayer;
    Player[] players;
    long[] hands;
    double pot;
    boolean isGameOver;
    int bettingRound;
    double[] reachProbability;
    int lastRaiser;

    GameTreeNode() {
        history = "";
        currentPlayer = 0;
        lastRaiser = -1;
    }

    GameTreeNode(GameTreeNode gameTreeNode) {
        this.hands = gameTreeNode.hands;
        this.players = gameTreeNode.players;
        this.pot = gameTreeNode.pot;
        this.currentPlayer = gameTreeNode.currentPlayer;
        this.history = gameTreeNode.history;
        this.reachProbability = gameTreeNode.reachProbability;
        this.lastRaiser = gameTreeNode.lastRaiser;
        this.bettingRound = gameTreeNode.bettingRound;
        this.isGameOver = gameTreeNode.isGameOver;
        this.cardInfoSets = gameTreeNode.cardInfoSets;
    }

    public static GameTreeNode headsUpNoLimitHoldEm(Random random) {
        GameTreeNode gameTreeNode = new GameTreeNode();
        gameTreeNode.initializeCardDeck(NUM_PLAYERS * 2 + 5, random);
        gameTreeNode.players = new Player[NUM_PLAYERS];
        gameTreeNode.reachProbability = new double[NUM_PLAYERS];
        for (int i = 0; i < NUM_PLAYERS; i++) {
            gameTreeNode.players[i] = new Player(100.0);
            gameTreeNode.reachProbability[i] = 1.0;
        }
        gameTreeNode.players[POSITION_SMALL_BLIND].pay(0.5);
        gameTreeNode.players[POSITION_BIG_BLIND].pay(1.0);
        gameTreeNode.pot = 1.5;
        gameTreeNode.currentPlayer = BETTING_ORDER_PER_ROUND[0][0];
        return gameTreeNode;
    }

    private void initializeCardDeck(int numCards, Random random) {
        Card[] deck = new Card[numCards];
        for (int i = 0; i < numCards; i++) {
            Card card;
            do {
                card = Card.randomCard(random);
            } while (cardAlreadyInDeck(deck, card, i));
            deck[i] = card;
        }

        hands = new long[NUM_PLAYERS];
        cardInfoSets = new String[NUM_BETTINGS_ROUNDS][NUM_PLAYERS];
        for (int i = 0; i < NUM_PLAYERS; i++) {
            int startIndex = 2 * i;

            CardInfoSetBuilder infoSetBuilder = new CardInfoSetBuilder();
            infoSetBuilder.appendPosition(i);
            infoSetBuilder.appendHoleCards(deck[startIndex], deck[startIndex+1]);
            cardInfoSets[0][i] = infoSetBuilder.toString();
            infoSetBuilder.appendFlop(deck[FLOP_CARD1], deck[FLOP_CARD2], deck[FLOP_CARD3]);
            cardInfoSets[1][i] = infoSetBuilder.toString();
            infoSetBuilder.appendCard(deck[TURN_CARD]);
            cardInfoSets[2][i] = infoSetBuilder.toString();
            infoSetBuilder.appendCard(deck[RIVER_CARD]);
            cardInfoSets[3][i] = infoSetBuilder.toString();

            List<Card> cards = new ArrayList<>(7);
            cards.add(deck[startIndex]);
            cards.add(deck[startIndex + 1]);
            cards.add(deck[FLOP_CARD1]);
            cards.add(deck[FLOP_CARD2]);
            cards.add(deck[FLOP_CARD3]);
            cards.add(deck[TURN_CARD]);
            cards.add(deck[RIVER_CARD]);
            hands[i] = Hand.of(cards).value;
        }
    }

    private boolean cardAlreadyInDeck(Card[] deck, Card card, int insertAtPosition) {
        for (int i = 0; i < insertAtPosition; i++) {
            if (deck[i] == card) {
                return true;
            }
        }
        return false;
    }

    public int currentPlayer() {
        return currentPlayer;
    }

    public double[] getRewards() {
        double[] winnings = new double[NUM_PLAYERS];
        List<Integer> playersStillInGame = new ArrayList<>();
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (!players[i].hasFolded) {
                playersStillInGame.add(i);
            }
            winnings[i] = -players[i].investment;
        }

        if (playersStillInGame.size() == 1) {
            winnings[playersStillInGame.get(0)] = pot - players[playersStillInGame.get(0)].investment;
            return winnings;
        }

        List<KeyValue<Integer, Long>> bestHands = new ArrayList<>();
        long maxValue = 0;
        for (int i = 0; i < NUM_PLAYERS; i++) {
            long hand = hands[i];
            if (hand > maxValue) {
                bestHands.add(new KeyValue<>(i, hand));
                maxValue = hand;
            }
        }

        List<Integer> winners = new ArrayList<>();

        for (KeyValue<Integer, Long> bestHand : bestHands) {
            if (bestHand.value() == maxValue) {
                winners.add(bestHand.key());
            }
        }

        double sharedPot = pot / winners.size();
        for (int i = 0; i < winners.size(); i++) {
            winnings[winners.get(i)] = sharedPot - players[winners.get(i)].investment;
        }
        return winnings;
    }

    public boolean isTerminal() {
        return isGameOver;
    }

    public String infoSet() {
        StringBuilder sb = new StringBuilder();
        sb.append(cardInfoSets[bettingRound][currentPlayer]);
        sb.append(history);
        return sb.toString();
    }

    public int legalActions() {
        int numActions = 0;
        if (isFoldLegal()) {
            numActions++;
        }
        if (isCallLegal()) {
            numActions++;
        }
        if (isRaiseLegal()) {
            numActions++;
        }
        return numActions;
    }

    private boolean isRaiseLegal() {
        double maxInvestment = -1;
        for (Player player : players) {
            maxInvestment = Math.max(player.investment, maxInvestment);
        }
        double currentPlayerInvestment = players[currentPlayer].investment;
        double payment = maxInvestment - currentPlayerInvestment;
        return payment < players[currentPlayer].stack;
    }

    private boolean isCallLegal() {
        return true;
    }

    private boolean isFoldLegal() {
        double maxInvestment = -1;
        for (Player player : players) {
            maxInvestment = Math.max(player.investment, maxInvestment);
        }
        double currentPlayerInvestment = players[currentPlayer].investment;
        double payment = maxInvestment - currentPlayerInvestment;
        return payment > 0;
    }

    public Node toNode(String infoSet) {
        return new Node(infoSet, legalActions());
    }

    public void takeAction(int actionId) {
        if (isFoldLegal()) {
            if (actionId == ACTION_FOLD) {
                fold();
                return;
            }
        } else {
            actionId++;
        }
        if (isCallLegal()) {
            if (actionId == ACTION_CALL) {
                call();
                return;
            }
        } else {
            actionId++;
        }
        if (isRaiseLegal()) {
            if (actionId == ACTION_ALL_IN) {
                raise(players[currentPlayer].stack);
                return;
            }
        } else {
            actionId++;
        }
        throw new IllegalStateException();
    }

    public GameTreeNode takeAction(int actionId, double probability) {
        GameTreeNode next = new GameTreeNode(this);
        next.players = Arrays.copyOf(next.players, next.players.length);
        next.players[next.currentPlayer] = new Player(next.players[next.currentPlayer]);
        next.takeAction(actionId);
        next.reachProbability = Arrays.copyOf(next.reachProbability, next.reachProbability.length);
        next.reachProbability[currentPlayer] *= probability;
        next.determineNextPlayer();
        return next;
    }

    private void raise(double amount) {
        players[currentPlayer].pay(amount);
        pot += amount;
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (BETTING_ORDER_PER_ROUND[bettingRound][i] == currentPlayer) {
                lastRaiser = i;
                break;
            }
        }
        history += "r" + amount;
    }

    private void fold() {
        players[currentPlayer].fold();
        history += "f";
    }

    private void call() {
        double maxInvestment = -1;
        for (int i = 0; i < players.length; i++) {
            maxInvestment = Math.max(players[i].investment, maxInvestment);
        }
        double currentPlayerInvestment = players[currentPlayer].investment;
        double payment = maxInvestment - currentPlayerInvestment;
        players[currentPlayer].pay(payment);
        pot += payment;
        history += "c";
    }

    private void determineNextPlayer() {
        int numPlayerStillInGame = 0;
        int numPlayersAllIn = 0;
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (!players[i].hasFolded) {
                numPlayerStillInGame++;
            }
            if (players[i].stack <= 0) {
                numPlayersAllIn++;
            }
        }

        if (numPlayerStillInGame == 1) {
            isGameOver = true;
            return;
        }
        if (numPlayersAllIn == numPlayerStillInGame) {
            isGameOver = true;
            bettingRound = ROUND_RIVER;
            return;
        }

        boolean isEndOfBettingRound = false;
        if (lastRaiser < 0) {
            // Noone raised this round
            for (int i = NUM_PLAYERS-1; i > 0; i--) {
                if (!players[BETTING_ORDER_PER_ROUND[bettingRound][i]].hasFolded) {
                    isEndOfBettingRound = currentPlayer == BETTING_ORDER_PER_ROUND[bettingRound][i];
                    break;
                }
            }
        } else {
            // Someone raised and that was one round ago
            int lastRaiserIndex = -1;
            for (int i = 0; i < NUM_PLAYERS; i++) {
                if (BETTING_ORDER_PER_ROUND[bettingRound][i] == lastRaiser) {
                    lastRaiserIndex = i;
                }
            }
            int playerEndingRoundIndex = lastRaiserIndex <= 0 ? NUM_PLAYERS-1 : lastRaiserIndex - 1;
            while (players[BETTING_ORDER_PER_ROUND[bettingRound][playerEndingRoundIndex]].hasFolded) {
                playerEndingRoundIndex = playerEndingRoundIndex <= 0 ? NUM_PLAYERS-1 : playerEndingRoundIndex - 1;
            }
            isEndOfBettingRound = currentPlayer == BETTING_ORDER_PER_ROUND[bettingRound][playerEndingRoundIndex];
        }

        int currentPlayerIndex = -1;
        for (int i = 0; i < players.length; i++) {
            if (BETTING_ORDER_PER_ROUND[bettingRound][i] == currentPlayer) {
                currentPlayerIndex = i;
            }
        }

        if (isEndOfBettingRound) {
            switch (bettingRound) {
                case ROUND_PRE_FLOP: {
                    nextBettingRound(ROUND_POST_FLOP);
                    break;
                }
                case ROUND_POST_FLOP: {
                    nextBettingRound(ROUND_TURN);
                    break;
                }
                case ROUND_TURN: {
                    nextBettingRound(ROUND_RIVER);
                    break;
                }
                case ROUND_RIVER: {
                    isGameOver = true;
                    break;
                }
            }
        } else {
            int nextPlayerIndex = currentPlayerIndex;
            do {
                nextPlayerIndex = (nextPlayerIndex + 1) % NUM_PLAYERS;

                currentPlayer = BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex];
            } while (players[currentPlayer].hasFolded && players[currentPlayer].stack > 0);
        }
    }

    private void nextBettingRound(int nextBettingRound) {
        bettingRound = nextBettingRound;
        lastRaiser = -1;

        int nextPlayerIndex = 0;
        while (players[BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex]].hasFolded && players[BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex]].stack > 0) {
            assert nextPlayerIndex < 5 : "Oops, something went wrong when determining the next player";
            nextPlayerIndex++;
        }
        currentPlayer = BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex];
    }

    public double reachProbability() {
        return reachProbability[currentPlayer];
    }

    public double reachProbabiltyForRegret() {
        double probability = 1.0;
        for (int i = 0; i < reachProbability.length; i++) {
            if (i != currentPlayer) {
                probability *= reachProbability[i];
            }
        }
        return probability;
    }
}
