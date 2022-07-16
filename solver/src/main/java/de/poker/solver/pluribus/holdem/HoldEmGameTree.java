package de.poker.solver.pluribus.holdem;

import de.poker.solver.game.Card;
import de.poker.solver.game.CardUtils;
import de.poker.solver.game.Hand;
import de.poker.solver.pluribus.GameTree;
import de.poker.solver.utility.CardInfoSetBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HoldEmGameTree implements GameTree<String> {
    private static final int POSITION_SMALL_BLIND = 0;
    private static final int POSITION_BIG_BLIND = 1;
    private static final int ACTION_FOLD = 0;
    private static final int ACTION_CALL = 1;
    private static final int ACTION_50_P_POT = 2;
    private static final int ACTION_100_P_POT = 3;
    private static final int ACTION_200_P_POT = 4;

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

    String history = "";
    Long[][] cardInfoSets;
    int currentPlayer;
    boolean[] playersWhoFolded = new boolean[NUM_PLAYERS];
    int[] playersInvestment = new int[NUM_PLAYERS];
    int[] playersStack = new int[NUM_PLAYERS];
    long[] playersHands = new long[NUM_PLAYERS];
    int pot;
    boolean isGameOver;
    int bettingRound;
    int lastRaiser;


    public HoldEmGameTree(Card[] deck) {
        Arrays.fill(playersStack, 10000);
        pay(POSITION_SMALL_BLIND, 50);
        pay(POSITION_BIG_BLIND, 100);
        currentPlayer = BETTING_ORDER_PER_ROUND[0][0];
        cardInfoSets = new Long[NUM_BETTINGS_ROUNDS][NUM_PLAYERS];
        for (int i = 0; i < NUM_PLAYERS; i++) {
            int startIndex = 2 * i;

            CardInfoSetBuilder infoSetBuilder = new CardInfoSetBuilder();
            infoSetBuilder.appendPosition(i);
            infoSetBuilder.appendHoleCards(deck[startIndex], deck[startIndex+1]);
            List<Card> cards = new ArrayList<>(7);
            cards.add(deck[startIndex]);
            cards.add(deck[startIndex+1]);
            cards.add(deck[FLOP_CARD1]);
            cards.add(deck[FLOP_CARD2]);
            cards.add(deck[FLOP_CARD3]);
            cards.add(deck[TURN_CARD]);
            cards.add(deck[RIVER_CARD]);
            CardUtils.normalizeInPlace(cards);
            cardInfoSets[0][i] = CardUtils.cardsToLong(cards, 2);
            cardInfoSets[1][i] = CardUtils.cardsToLong(cards, 5);
            cardInfoSets[2][i] = CardUtils.cardsToLong(cards, 6);
            cardInfoSets[3][i] = CardUtils.cardsToLong(cards, 7);

            playersHands[i] = Hand.of(cards).value;
        }
    }
    private HoldEmGameTree(HoldEmGameTree copy) {
        this.history = copy.history;
        this.cardInfoSets = copy.cardInfoSets;
        this.currentPlayer = copy.currentPlayer;
        this.playersWhoFolded = copy.playersWhoFolded;
        this.playersInvestment = copy.playersInvestment;
        this.playersStack = copy.playersStack;
        this.playersHands = copy.playersHands;
        this.pot = copy.pot;
        this.isGameOver = copy.isGameOver;
        this.bettingRound = copy.bettingRound;
        this.lastRaiser = copy.lastRaiser;
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
    public int actions() {
        int numActions = 0;
        if (isFoldLegal()) {
            numActions++;
        }
        if (isCallLegal()) {
            numActions++;
        }
        numActions += 3;
        return numActions;
    }

    private boolean isRaiseLegal(int amount) {
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
        HoldEmGameTree next = new HoldEmGameTree(this);
        if (!isFoldLegal()) {
            actionId++;
        }
        if (!isCallLegal()) {
            actionId++;
        }
        switch (actionId) {
            case ACTION_FOLD -> {
                next.fold();
                break;
            }
            case ACTION_CALL -> {
                next.call();
                break;
            }
            case ACTION_50_P_POT -> {
                next.raise(pot / 2);
                break;
            }
            case ACTION_100_P_POT -> {
                next.raise(pot);
                break;
            }
            case ACTION_200_P_POT -> {
                next.raise(pot * 2);
                break;
            }
            default -> throw new IllegalArgumentException();
        }
        next.determineNextPlayer();
        return next;
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

        int nextPlayerIndex = 0;
        while (hasFolded(BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex]) && getStack(BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex]) <= 0) {
            nextPlayerIndex++;
        }
        currentPlayer = BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex];
    }

    private void raise(int amount) {
        int paid = pay(currentPlayer, amount);
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (BETTING_ORDER_PER_ROUND[bettingRound][i] == currentPlayer) {
                lastRaiser = i;
                break;
            }
        }
        history += "r" + paid;
    }

    private int pay(int playerId, int amount) {
        amount = Math.min(playersStack[playerId], amount);
        playersStack = Arrays.copyOf(playersStack, NUM_PLAYERS);
        playersInvestment = Arrays.copyOf(playersInvestment, NUM_PLAYERS);
        playersStack[playerId] -= amount;
        playersInvestment[playerId] += amount;
        pot += amount;
        return amount;
    }

    private void fold() {
        playersWhoFolded = Arrays.copyOf(playersWhoFolded, NUM_PLAYERS);
        playersWhoFolded[currentPlayer] = true;
        history += "f";
    }

    private void call() {
        int maxInvestment = -1;
        for (int p = 0; p < NUM_PLAYERS; p++) {
            maxInvestment = Math.max(getInvestment(p), maxInvestment);
        }
        int payment = maxInvestment - getInvestment(currentPlayer);
        pay(currentPlayer, payment);
        history += "c";
    }

    @Override
    public boolean shouldUpdateRegrets() {
        return true;
    }
}
