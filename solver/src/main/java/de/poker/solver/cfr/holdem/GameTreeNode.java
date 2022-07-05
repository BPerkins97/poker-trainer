package de.poker.solver.cfr.holdem;

import de.poker.solver.cfr.Card;
import de.poker.solver.utility.KeyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// TODO Reduced Hole Cards set, when preflop
// TODO Order Flop cards in order to reduce space size
public class GameTreeNode {
    private static final int POSITION_SMALL_BLIND = 0;
    private static final int POSITION_BIG_BLIND = 1;
    private static final int POSITION_LO_JACK = 2;
    private static final int POSITION_HI_JACK = 3;
    private static final int POSITION_CUT_OFF = 4;
    private static final int POSITION_BUTTON = 5;
    private static final int ACTION_FOLD = 0;
    private static final int ACTION_CALL = 1;
    private static final int ACTION_ALL_IN = 2;

    private static final int ROUND_PRE_FLOP = 0;
    private static final int ROUND_POST_FLOP = 1;
    private static final int ROUND_TURN = 2;
    private static final int ROUND_RIVER = 3;

    private static final int[][] BETTING_ORDER_PER_ROUND = new int[4][];

    static {
        BETTING_ORDER_PER_ROUND[0] = new int[]{2, 3, 4, 5, 0, 1};
        BETTING_ORDER_PER_ROUND[1] = new int[]{0, 1, 2, 3, 4, 5};
        BETTING_ORDER_PER_ROUND[2] = new int[]{0, 1, 2, 3, 4, 5};
        BETTING_ORDER_PER_ROUND[3] = new int[]{0, 1, 2, 3, 4, 5};
    }

    String history;
    String communityCards;
    String[] holeCards;
    int currentPlayer;
    Player[] players;
    Hand[] hands;
    Card[] deck;
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
        this.communityCards = gameTreeNode.communityCards;
        this.holeCards = gameTreeNode.holeCards;
    }

    public static GameTreeNode noLimitHoldEm6Max(Random random) {
        GameTreeNode gameTreeNode = new GameTreeNode();
        gameTreeNode.initializeCardDeck(17, random);
        gameTreeNode.players = new Player[6];
        gameTreeNode.reachProbability = new double[6];
        for (int i = 0; i < 6; i++) {
            gameTreeNode.players[i] = new Player(100.0);
            gameTreeNode.reachProbability[i] = 1.0;
        }
        gameTreeNode.players[POSITION_SMALL_BLIND].pay(0.5);
        gameTreeNode.players[POSITION_BIG_BLIND].pay(1.0);
        gameTreeNode.pot = 1.5;
        gameTreeNode.currentPlayer = BETTING_ORDER_PER_ROUND[0][0];
        gameTreeNode.communityCards = "";
        return gameTreeNode;
    }

    private void initializeCardDeck(int numCards, Random random) {
        deck = new Card[numCards];
        for (int i = 0; i < numCards; i++) {
            Card card;
            do {
                card = Card.randomCard(random);
            } while (cardAlreadyInDeck(card, i));
            deck[i] = card;
        }

        hands = new Hand[6];
        holeCards = new String[6];
        for (int i=0;i<6;i++) {
            int startIndex = 2 * i;
            List<Card> cards = new ArrayList<>(7);
            cards.add(deck[startIndex]);
            cards.add(deck[startIndex+1]);
            cards.add(deck[12]);
            cards.add(deck[13]);
            cards.add(deck[14]);
            cards.add(deck[15]);
            cards.add(deck[16]);
            hands[i] = Hand.of(cards);

            holeCards[i] = switch (currentPlayer) {
                case POSITION_SMALL_BLIND -> "Small Blind";
                case POSITION_BIG_BLIND -> "Big Blind";
                case POSITION_LO_JACK -> "Lo Jack";
                case POSITION_HI_JACK -> "Hi Jack";
                case POSITION_CUT_OFF -> "Cut off";
                case POSITION_BUTTON -> "Button";
                default -> throw new IllegalStateException();
            } + deck[startIndex] + deck[startIndex+1];
        }
    }

    private boolean cardAlreadyInDeck(Card card, int insertAtPosition) {
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
        double[] winnings = new double[6];
        List<Integer> playersStillInGame = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
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
        for (int i = 0; i < 6; i++) {
            Hand of = hands[i];
            if (of.value > maxValue) {
                bestHands.add(new KeyValue<>(i, of.value));
                maxValue = of.value;
            }
        }

        List<Integer> winners = new ArrayList<>();

        for (int i = 0; i < bestHands.size(); i++) {
            if (bestHands.get(i).value() == maxValue) {
                winners.add(bestHands.get(i).key());
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
        sb.append(holeCards[currentPlayer]);
        if (bettingRound >= ROUND_POST_FLOP) {
            sb.append(communityCards);
        }
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
        return true;
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
        next.deck = deck;
        next.takeAction(actionId);
        next.reachProbability = Arrays.copyOf(next.reachProbability, next.reachProbability.length);
        next.reachProbability[currentPlayer] *= probability;
        next.determineNextPlayer();
        return next;
    }

    private void raise(double amount) {
        players[currentPlayer].pay(amount);
        pot += amount;
        for (int i = 0; i < 6; i++) {
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
        for (int i = 0; i < 6; i++) {
            if (!players[i].hasFolded) {
                numPlayerStillInGame++;
            }
        }

        boolean isEndOfBettingRound = false;
        if (lastRaiser < 0) {
            // Noone raised this round
            for (int i = 5; i > 0; i--) {
                if (!players[BETTING_ORDER_PER_ROUND[bettingRound][i]].hasFolded) {
                    isEndOfBettingRound = currentPlayer == BETTING_ORDER_PER_ROUND[bettingRound][i];
                    break;
                }
            }
        } else {
            // Someone raised and that was one round ago
            int lastRaiserIndex = -1;
            for (int i = 0; i < 6; i++) {
                if (BETTING_ORDER_PER_ROUND[bettingRound][i] == lastRaiser) {
                    lastRaiserIndex = i;
                }
            }
            int playerEndingRoundIndex = lastRaiserIndex <= 0 ? 5 : lastRaiserIndex - 1;
            while (players[BETTING_ORDER_PER_ROUND[bettingRound][playerEndingRoundIndex]].hasFolded) {
                playerEndingRoundIndex = playerEndingRoundIndex <= 0 ? 5 : playerEndingRoundIndex - 1;
            }
            isEndOfBettingRound = currentPlayer == BETTING_ORDER_PER_ROUND[bettingRound][playerEndingRoundIndex];
        }

        if (numPlayerStillInGame == 1) {
            isGameOver = true;
            return;
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
                    communityCards += deck[12].toString() + deck[13].toString() + deck[14].toString();
                    break;
                }
                case ROUND_POST_FLOP: {
                    nextBettingRound(ROUND_TURN);
                    communityCards += deck[15].toString();
                    break;
                }
                case ROUND_TURN: {
                    nextBettingRound(ROUND_RIVER);
                    communityCards += deck[16].toString();
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
                nextPlayerIndex = nextPlayerIndex >= 5 ? 0 : nextPlayerIndex + 1;

                currentPlayer = BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex];
            } while (players[currentPlayer].hasFolded);
        }
    }

    private void nextBettingRound(int nextBettingRound) {
        bettingRound = nextBettingRound;
        lastRaiser = -1;

        int nextPlayerIndex = 0;
        while (players[BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex]].hasFolded) {
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
