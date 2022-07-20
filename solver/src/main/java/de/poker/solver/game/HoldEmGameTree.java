package de.poker.solver.game;

import de.poker.solver.utility.CardInfoSetBuilder;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class HoldEmGameTree implements Cloneable {
    private static final int STACK_BITMASK = 0xffff;

    private static final Action[] ACTIONS = new Action[102];
    private static final int POSITION_SMALL_BLIND = 0;
    private static final int POSITION_BIG_BLIND = 1;

    private static final byte ROUND_PRE_FLOP = 0;
    private static final byte ROUND_POST_FLOP = 1;
    private static final byte ROUND_TURN = 2;
    private static final byte ROUND_RIVER = 3;

    private static final int[][] BETTING_ORDER_PER_ROUND = new int[Constants.NUM_BETTING_ROUNDS][Constants.NUM_PLAYERS];

    private static final int FLOP_CARD1;
    private static final int FLOP_CARD2;
    private static final int FLOP_CARD3;
    private static final int TURN_CARD;
    private static final int RIVER_CARD;
    public static final int BETTING_ROUND_PRE_FLOP = 0;
    public static final int BETTING_ROUND_FLOP = 1;
    public static final int BETTING_ROUND_TURN = 2;
    public static final int BETTING_ROUND_RIVER = 3;
    public static final int ACTION_ID_FOLD = 100;
    public static final int ACTION_ID_CALL = 101;

    static {
        for (int i = 0; i< Constants.NUM_PLAYERS; i++) {
            BETTING_ORDER_PER_ROUND[0][i] = (i + 2) % Constants.NUM_PLAYERS;
            BETTING_ORDER_PER_ROUND[1][i] = i;
            BETTING_ORDER_PER_ROUND[2][i] = i;
            BETTING_ORDER_PER_ROUND[3][i] = i;
        }
        FLOP_CARD1 = Constants.NUM_PLAYERS * 2;
        FLOP_CARD2 = Constants.NUM_PLAYERS * 2 + 1;
        FLOP_CARD3 = Constants.NUM_PLAYERS * 2 + 2;
        TURN_CARD = Constants.NUM_PLAYERS * 2 + 3;
        RIVER_CARD = Constants.NUM_PLAYERS * 2 + 4;
        for (int i = Constants.BIG_BLIND; i< Constants.STARTING_STACK_SIZE; i+= Constants.BIG_BLIND) {
            ACTIONS[i / 100] = Action.raise(i);
        }
        ACTIONS[100] = Action.fold();
        ACTIONS[101] = Action.call();
    }

    public String history = "";
    public String[][] cardInfoSets;
    public int currentPlayer;
    public byte playersWhoFolded;
    public int playersStacks;
    public int playerInvestments;
    public long[] playersHands = new long[Constants.NUM_PLAYERS];
    public int pot;
    public boolean isGameOver;
    public byte bettingRound;
    public int lastRaiser;
    public int amountLastRaised;
    public int[] actionIds;
    public int numActions;


    public HoldEmGameTree(Card[] deck) {
        setPlayerStack(0, Constants.STARTING_STACK_SIZE);
        setPlayerStack(1, Constants.STARTING_STACK_SIZE);
        pay(POSITION_SMALL_BLIND, Constants.SMALL_BLIND);
        pay(POSITION_BIG_BLIND, Constants.BIG_BLIND);
        currentPlayer = BETTING_ORDER_PER_ROUND[0][0];
        cardInfoSets = new String[Constants.NUM_BETTING_ROUNDS][Constants.NUM_PLAYERS];
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
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
            playersHands[i] = HandEvaluator.of(cards);
        }
        setNextActions();
    }

    public static HoldEmGameTree getRandomRootState() {
        int numCards = Constants.NUM_PLAYERS * 2 + 5;
        Card[] deck = new Card[numCards];
        for (int i = 0; i < numCards; i++) {
            Card card;
            do {
                card = Card.randomCard(ThreadLocalRandom.current());
            } while (cardAlreadyInDeck(deck, card, i));
            deck[i] = card;
        }
        return new HoldEmGameTree(deck);
    }

    public static boolean cardAlreadyInDeck(Card[] deck, Card card, int insertAtPosition) {
        for (int i = 0; i < insertAtPosition; i++) {
            if (deck[i] == card) {
                return true;
            }
        }
        return false;
    }

    public boolean isTerminalForPlayer() {
        return isGameOver || playersWhoFolded > 0;
    }

    private boolean hasFolded(int playerId) {
        return (playersWhoFolded & 1 << playerId) > 0;
    }

    public int getPayoffForPlayer(int playerId) {
        if (hasFolded(playerId)) {
            return -getInvestment(playerId);
        }

        long bestHandValue = Long.MIN_VALUE;
        for (int p = 0; p< Constants.NUM_PLAYERS; p++) {
            bestHandValue = Math.max(bestHandValue, playersHands[p]);
        }

        if (playersHands[playerId] == bestHandValue) {
            return pot - getInvestment(playerId);
        } else {
            return -getInvestment(playerId);
        }
    }

    private int getInvestment(int playerId) {
        return (STACK_BITMASK << (16 * playerId) & playerInvestments) >> (16 * playerId);
    }

    public boolean isCurrentPlayer(int playerId) {
        return currentPlayer == playerId;
    }

    public String asInfoSet(int playerId) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<=bettingRound;i++) {
            sb.append(cardInfoSets[bettingRound][playerId]).append("|");
        }
        sb.append(history);
        return sb.toString();
    }

    public int numActions() {
        return numActions;
    }

    private boolean isRaiseLegal(int amount) {
        return amount > Constants.BIG_BLIND && amount > amountLastRaised && amount < getStack(currentPlayer);
    }

    private int getStack(int playerId) {
        return (STACK_BITMASK << (16 * playerId) & playersStacks) >> (16 * playerId);
    }

    private boolean isCallLegal() {
        return true;
    }

    private boolean isFoldLegal() {
        int maxInvestment = -1;
        for (int p = 0; p< Constants.NUM_PLAYERS; p++) {
            maxInvestment = Math.max(getInvestment(p), maxInvestment);
        }
        int payment = maxInvestment - getInvestment(currentPlayer);
        return payment > 0;
    }

    public HoldEmGameTree takeAction(int actionId) {
        HoldEmGameTree next = null;
        try {
            next = (HoldEmGameTree)this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        Action action = ACTIONS[actionIds[actionId]];
        if (action.isFold()) {
            next.fold();
        } else if (action.isCall()) {
            next.call();
        } else {
            next.raise(action.amount());
        }
        next.determineNextPlayer();
        next.setNextActions();
        next.history += action.presentation();
        return next;
    }

    private void setNextActions() {
        this.actionIds = new int[10];
        this.numActions = 0;
        switch (bettingRound) {
            case BETTING_ROUND_PRE_FLOP:
                getPreFlopActions();
                break;
            case BETTING_ROUND_FLOP:
                getFlopActions();
                break;
            case BETTING_ROUND_TURN:
                getPostFlopActions();
                break;
            case BETTING_ROUND_RIVER:
                getPostFlopActions();
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void getPostFlopActions() {
        if (isFoldLegal()) {
            addAction(ACTION_ID_FOLD);
        }
        addAction(ACTION_ID_CALL);
        addRaiseIfLegal(pot / 2);
        addRaiseIfLegal(pot);
        addRaiseIfLegal((int)(1.5 * pot));
        addRaiseIfLegal(getStack(currentPlayer));
    }

    private void addAction(int actionId) {
        actionIds[numActions] = actionId;
        numActions++;
    }

    private void getFlopActions() {
        if (isFoldLegal()) {
            addAction(ACTION_ID_FOLD);
        }
        if (isCallLegal()) {
            addAction(ACTION_ID_CALL);
        }
        addRaiseIfLegal(pot / 4);
        addRaiseIfLegal(pot / 3);
        addRaiseIfLegal(pot / 2);
        addRaiseIfLegal(pot);
        addRaiseIfLegal((int)(1.25 * pot));
        addRaiseIfLegal((int)(1.5 * pot));
        addRaiseIfLegal((int)(2.0 * pot));
        addRaiseIfLegal(getStack(currentPlayer));
    }

    private void getPreFlopActions() {
        if (isFoldLegal()) {
            addAction(ACTION_ID_FOLD);
        }
        if (isCallLegal()) {
            addAction(ACTION_ID_CALL);
        }
        boolean isFirstRaise = amountLastRaised <= 0;
        if (isFirstRaise) {
            addRaiseIfLegal(Constants.BIG_BLIND);
            addRaiseIfLegal((int)(2.0 * Constants.BIG_BLIND));
            addRaiseIfLegal((int)(3.0 * Constants.BIG_BLIND));
            addRaiseIfLegal((int)(4.0 * Constants.BIG_BLIND));
            addRaiseIfLegal((int)(5.0 * Constants.BIG_BLIND));
        } else {
            addRaiseIfLegal(pot / 4);
            addRaiseIfLegal(pot / 3);
            addRaiseIfLegal(pot / 2);
            addRaiseIfLegal(pot);
            addRaiseIfLegal((int)(1.25 * pot));
            addRaiseIfLegal((int)(1.5 * pot));
            addRaiseIfLegal((int)(2.0 * pot));
        }
        addRaiseIfLegal(getStack(currentPlayer));
    }

    private void addRaiseIfLegal(int raiseAmount) {
        if (isRaiseLegal(raiseAmount)) {
            addAction(raiseAmount / Constants.BIG_BLIND);
        }
    }

    private void determineNextPlayer() {
        int numPlayerStillInGame = 0;
        int numPlayersAllIn = 0;
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
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
            for (int i = Constants.NUM_PLAYERS -1; i > 0; i--) {
                if (!hasFolded(BETTING_ORDER_PER_ROUND[bettingRound][i])) {
                    isEndOfBettingRound = currentPlayer == BETTING_ORDER_PER_ROUND[bettingRound][i];
                    break;
                }
            }
        } else {
            // Someone raised and that was one round ago
            int lastRaiserIndex = -1;
            for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
                if (BETTING_ORDER_PER_ROUND[bettingRound][i] == lastRaiser) {
                    lastRaiserIndex = i;
                }
            }
            int playerEndingRoundIndex = lastRaiserIndex <= 0 ? Constants.NUM_PLAYERS -1 : lastRaiserIndex - 1;
            while (hasFolded(BETTING_ORDER_PER_ROUND[bettingRound][playerEndingRoundIndex])) {
                playerEndingRoundIndex = playerEndingRoundIndex <= 0 ? Constants.NUM_PLAYERS -1 : playerEndingRoundIndex - 1;
            }
            isEndOfBettingRound = currentPlayer == BETTING_ORDER_PER_ROUND[bettingRound][playerEndingRoundIndex];
        }

        int currentPlayerIndex = -1;
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
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
                nextPlayerIndex = (nextPlayerIndex + 1) % Constants.NUM_PLAYERS;

                currentPlayer = BETTING_ORDER_PER_ROUND[bettingRound][nextPlayerIndex];
            } while (hasFolded(currentPlayer) || getStack(currentPlayer) <= 0);
        }
    }

    private void nextBettingRound(byte nextBettingRound) {
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
        assert amount <= getStack(currentPlayer);
        pay(currentPlayer, amount);
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            if (BETTING_ORDER_PER_ROUND[bettingRound][i] == currentPlayer) {
                lastRaiser = i;
                break;
            }
        }
        amountLastRaised = amount - amountLastRaised;
    }

    private void pay(int playerId, int amount) {
        assert amount <= getStack(currentPlayer);
        assert amount > 0;
        subtractFromPlayerStack(playerId, amount);
        addToPlayerInvestment(playerId, amount);
        pot += amount;
    }

    private void subtractFromPlayerStack(int playerId, int amount) {
        playersStacks -= amount << (16 * playerId);
    }

    private void addToPlayerInvestment(int playerId, int amount) {
        playerInvestments += amount << (16 * playerId);
    }

    private void setPlayerStack(int playerId, int stack) {
        playersStacks = ((~0 ^ (STACK_BITMASK << 16 * playerId)) & playersStacks) | (stack << (16 * playerId));
    }

    private void fold() {
        playersWhoFolded = (byte) (1 << currentPlayer);
    }

    private void call() {
        int maxInvestment = -1;
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            maxInvestment = Math.max(getInvestment(p), maxInvestment);
        }
        int payment = maxInvestment - getInvestment(currentPlayer);
        if (payment > 0) {
            pay(currentPlayer, payment);
        }
    }

    public boolean shouldUpdateRegrets() {
        return true;
    }
}
