package de.poker.solver.pluribus.holdem;

import de.poker.solver.game.Action;
import de.poker.solver.game.Card;
import de.poker.solver.game.Hand;
import de.poker.solver.pluribus.GameTree;
import de.poker.solver.utility.CardInfoSetBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HoldEmGameTree implements GameTree<String>, Cloneable {
    private static final int POSITION_SMALL_BLIND = 0;
    private static final int POSITION_BIG_BLIND = 1;

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
    public static final int ACTION_BIG_BLIND = 100;
    public static final int ACTION_SMALL_BLIND = 50;

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

    String history = "";
    String[][] cardInfoSets;
    int currentPlayer;
    boolean[] playersWhoFolded = new boolean[NUM_PLAYERS];
    int[] playersInvestment = new int[NUM_PLAYERS];
    int[] playersStack = new int[NUM_PLAYERS];
    long[] playersHands = new long[NUM_PLAYERS];
    int pot;
    boolean isGameOver;
    int bettingRound;
    int lastRaiser;
    int amountLastRaised;
    Action[] actions;


    public HoldEmGameTree(Card[] deck) {
        Arrays.fill(playersStack, 10000);
        pay(POSITION_SMALL_BLIND, ACTION_SMALL_BLIND);
        pay(POSITION_BIG_BLIND, ACTION_BIG_BLIND);
        currentPlayer = BETTING_ORDER_PER_ROUND[0][0];
        cardInfoSets = new String[NUM_BETTINGS_ROUNDS][NUM_PLAYERS];
        for (int i = 0; i < NUM_PLAYERS; i++) {
            int startIndex = 2 * i;

            CardInfoSetBuilder infoSetBuilder = new CardInfoSetBuilder();
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
            playersHands[i] = Hand.of(cards).value;
        }
        setNextActions();
    }

    @Override
    public boolean isTerminalForPlayer(int playerId) {
        return isGameOver || hasFolded(playerId);
    }

    private boolean hasFolded(int playerId) {
        return playersWhoFolded[playerId];
    }

    @Override
    public int getPayoffForPlayer(int playerId) {
        if (hasFolded(playerId)) {
            return -getInvestment(playerId);
        }

        long bestHandValue = Long.MIN_VALUE;
        for (int p=0;p<NUM_PLAYERS;p++) {
            bestHandValue = Math.max(bestHandValue, playersHands[p]);
        }

        if (playersHands[playerId] == bestHandValue) {
            return pot - getInvestment(playerId);
        } else {
            return -getInvestment(playerId);
        }
    }

    private int getInvestment(int playerId) {
        return playersInvestment[playerId];
    }

    @Override
    public boolean isCurrentPlayer(int playerId) {
        return currentPlayer == playerId;
    }

    @Override
    public String asInfoSet(int playerId) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<=bettingRound;i++) {
            sb.append(cardInfoSets[bettingRound][playerId]).append("|");
        }
        sb.append(history);
        return sb.toString();
    }

    @Override
    public int numActions() {
        return actions.length;
    }

    private boolean isRaiseLegal(int amount) {
        if (amount < ACTION_BIG_BLIND || amount < amountLastRaised || amount > playersStack[currentPlayer]) {
            return false;
        }
        int maxInvestment = -1;
        for (int p=0;p<NUM_PLAYERS;p++) {
            maxInvestment = Math.max(getInvestment(p), maxInvestment);
        }
        int payment = maxInvestment - getInvestment(currentPlayer);
        return payment <= getStack(currentPlayer);
    }

    private int getStack(int playerId) {
        return playersStack[playerId];
    }

    private boolean isCallLegal() {
        return true;
    }

    private boolean isFoldLegal() {
        int maxInvestment = -1;
        for (int p=0;p<NUM_PLAYERS;p++) {
            maxInvestment = Math.max(getInvestment(p), maxInvestment);
        }
        int payment = maxInvestment - getInvestment(currentPlayer);
        return payment > 0;
    }

    @Override
    public GameTree takeAction(int actionId) {
        HoldEmGameTree next = null;
        try {
            next = (HoldEmGameTree)this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        Action action = actions[actionId];
        if (action.isFold()) {
            next.fold();
        } else if (action.isCall()) {
            next.call();
        } else {
            next.raise(action.amount());
        }
        next.determineNextPlayer();
        next.setNextActions();
        next.history += action.asString();
        return next;
    }

    private void setNextActions() {
        List<Action> actions = new ArrayList<>();
        if (isFoldLegal()) {
            actions.add(Action.fold());
        }
        if (isCallLegal()) {
            actions.add(Action.call());
        }
        if (isRaiseLegal(pot)) {
            actions.add(Action.raise(pot));
        }
        actions.add(Action.raise(playersStack[currentPlayer]));
        this.actions = actions.toArray(Action[]::new);
    }

    private void determineNextPlayer() {
        int numPlayerStillInGame = 0;
        int numPlayersAllIn = 0;
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (!hasFolded(i)) {
                numPlayerStillInGame++;
            }
            if (getStack(i) <= 0) {
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
                if (!hasFolded(BETTING_ORDER_PER_ROUND[bettingRound][i])) {
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
            while (hasFolded(BETTING_ORDER_PER_ROUND[bettingRound][playerEndingRoundIndex])) {
                playerEndingRoundIndex = playerEndingRoundIndex <= 0 ? NUM_PLAYERS-1 : playerEndingRoundIndex - 1;
            }
            isEndOfBettingRound = currentPlayer == BETTING_ORDER_PER_ROUND[bettingRound][playerEndingRoundIndex];
        }

        int currentPlayerIndex = -1;
        for (int i = 0; i < NUM_PLAYERS; i++) {
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
            } while (hasFolded(currentPlayer) || getStack(currentPlayer) <= 0);
        }
    }

    private void nextBettingRound(int nextBettingRound) {
        bettingRound = nextBettingRound;
        lastRaiser = -1;
        amountLastRaised = 0;

        int nextPlayerIndex = 0;
        while (hasFolded(BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex]) && getStack(BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex]) <= 0) {
            nextPlayerIndex++;
        }
        currentPlayer = BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex];
    }

    private void raise(int amount) {
        assert amount > 0;
        assert amount <= playersStack[currentPlayer];
        pay(currentPlayer, amount);
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (BETTING_ORDER_PER_ROUND[bettingRound][i] == currentPlayer) {
                lastRaiser = i;
                break;
            }
        }
        amountLastRaised = amount - amountLastRaised;
    }

    private void pay(int playerId, int amount) {
        assert amount <= playersStack[playerId];
        assert amount > 0;
        playersStack = Arrays.copyOf(playersStack, NUM_PLAYERS);
        playersInvestment = Arrays.copyOf(playersInvestment, NUM_PLAYERS);
        playersStack[playerId] -= amount;
        playersInvestment[playerId] += amount;
        pot += amount;
    }

    private void fold() {
        playersWhoFolded = Arrays.copyOf(playersWhoFolded, NUM_PLAYERS);
        playersWhoFolded[currentPlayer] = true;
    }

    private void call() {
        int maxInvestment = -1;
        for (int p = 0; p < NUM_PLAYERS; p++) {
            maxInvestment = Math.max(getInvestment(p), maxInvestment);
        }
        int payment = maxInvestment - getInvestment(currentPlayer);
        if (payment > 0) {
            pay(currentPlayer, payment);
        }
    }

    @Override
    public boolean shouldUpdateRegrets() {
        return true;
    }
}
