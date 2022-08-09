package de.poker.solver.game;

import de.poker.solver.utility.CardInfoSetBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HoldEmGameTree implements Cloneable {
    private static final int NUM_BITS_FOR_STACK = 8;
    private static final long STACK_BITMASK = 0xff;

    private static final int POSITION_SMALL_BLIND = 0;
    private static final int POSITION_BIG_BLIND = 1;

    private static final byte ROUND_PRE_FLOP = 0;
    private static final byte ROUND_POST_FLOP = 1;
    private static final byte ROUND_TURN = 2;
    private static final byte ROUND_RIVER = 3;

    private static final byte[][] BETTING_ORDER_PER_ROUND = new byte[Constants.NUM_BETTING_ROUNDS][Constants.NUM_PLAYERS];

    private static final int FLOP_CARD1;
    private static final int FLOP_CARD2;
    private static final int FLOP_CARD3;
    private static final int TURN_CARD;
    private static final int RIVER_CARD;

    static {
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            BETTING_ORDER_PER_ROUND[0][i] = (byte) ((i + 2) % Constants.NUM_PLAYERS);
            BETTING_ORDER_PER_ROUND[1][i] = (byte) i;
            BETTING_ORDER_PER_ROUND[2][i] = (byte) i;
            BETTING_ORDER_PER_ROUND[3][i] = (byte) i;
        }
        FLOP_CARD1 = Constants.NUM_PLAYERS * 2;
        FLOP_CARD2 = Constants.NUM_PLAYERS * 2 + 1;
        FLOP_CARD3 = Constants.NUM_PLAYERS * 2 + 2;
        TURN_CARD = Constants.NUM_PLAYERS * 2 + 3;
        RIVER_CARD = Constants.NUM_PLAYERS * 2 + 4;
    }
    public byte currentPlayer;
    private short[] stacks;
    private short[] investments;
    private boolean[] folded;
    public byte winnersAtShowdown;
    public boolean isGameOver;
    public byte bettingRound;
    public int lastRaiser;
    public int amountLastRaised;
    private Card[][] holeCards;
    private Card[] communityCards;
    private Action actionTaken;
    public List<HoldEmGameTree> history;


    public HoldEmGameTree(Card[] deck) {
        stacks = new short[Constants.NUM_PLAYERS];
        investments = new short[Constants.NUM_PLAYERS];
        folded = new boolean[Constants.NUM_PLAYERS];
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            addToPlayerStack(i, Constants.STARTING_STACK_SIZE);
        }

        pay(POSITION_SMALL_BLIND, Constants.SMALL_BLIND);
        pay(POSITION_BIG_BLIND, Constants.BIG_BLIND);
        currentPlayer = BETTING_ORDER_PER_ROUND[0][0];
        lastRaiser = -1;
        holeCards = new Card[Constants.NUM_PLAYERS][2];
        long[] playersHands = new long[Constants.NUM_PLAYERS];
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            int startIndex = 2 * i;

            CardInfoSetBuilder infoSetBuilder = new CardInfoSetBuilder();
            infoSetBuilder.appendHoleCards(deck[startIndex], deck[startIndex + 1]);
            holeCards[i][0] = infoSetBuilder.getCard(0);
            holeCards[i][1] = infoSetBuilder.getCard(1);
            infoSetBuilder.appendFlop(deck[FLOP_CARD1], deck[FLOP_CARD2], deck[FLOP_CARD3]);
            infoSetBuilder.appendCard(deck[TURN_CARD]);
            infoSetBuilder.appendCard(deck[RIVER_CARD]);
            playersHands[i] = HandEvaluator.of(infoSetBuilder.cards());
        }
        communityCards = new Card[5];
        communityCards[0] = deck[FLOP_CARD1];
        communityCards[1] = deck[FLOP_CARD2];
        communityCards[2] = deck[FLOP_CARD3];
        communityCards[3] = deck[TURN_CARD];
        communityCards[4] = deck[RIVER_CARD];

        long bestHandValue = Long.MIN_VALUE;
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            bestHandValue = Math.max(bestHandValue, playersHands[p]);
        }

        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            if (bestHandValue == playersHands[i]) {
                addWinnerAtShowdown(i);
            }
        }
        history = new ArrayList<>();
        history.add(this);
    }

    public static double getPlayerStackRelativeToPot(int player, HoldEmGameTree gameState) {
        return 1.0 * gameState.getStack(player) / gameState.getPot();
    }

    public static double getActionAmountRelativeToPot(HoldEmGameTree gameState, Action action) {
        return 1.0 * action.amount() / gameState.getPot();
    }

    public static Action[] getPossibleActions(HoldEmGameTree gameState) {
        List<Action> actions = new ArrayList<>();
        if (gameState.isFoldLegal()) {
            actions.add(Action.fold());
        }
        actions.add(Action.call());
        int minRaise = minRaise(gameState);
        int maxRaise = maxRaise(gameState);
        if (maxRaise <= 0) {
            return actions.toArray(new Action[0]);
        }
        while (minRaise < maxRaise) {
            actions.add(Action.raise(minRaise));
            minRaise *= 1.5;
        }
        actions.add(Action.raise(maxRaise));
        return actions.toArray(new Action[0]);
    }

    private static int minRaise(HoldEmGameTree gameState) {
        return Math.max(gameState.amountLastRaised, Constants.BIG_BLIND);
    }

    private static int maxRaise(HoldEmGameTree state) {
        return state.getStack(state.currentPlayer) - state.getCallAmount();
    }

    public static double[] getPayOffs(HoldEmGameTree state) {
        double[] payOffs = new double[Constants.NUM_PLAYERS];
        for (int i=0;i<Constants.NUM_PLAYERS;i++) {
            payOffs[i] = state.getPayoffForPlayer(i);
        }
        return payOffs;
    }

    private void addWinnerAtShowdown(int playerId) {
        winnersAtShowdown += 1 << playerId;
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

    public boolean isGameOverForPlayer(int playerId) {
        return isGameOver || hasFolded(playerId);
    }

    private boolean hasFolded(int playerId) {
        return folded[playerId];
    }

    public int getPayoffForPlayer(int playerId) {
        assert isGameOverForPlayer(playerId);

        if (hasFolded(playerId)) {
            return -getInvestment(playerId);
        }

        int numPlayersWhoFolded = getNumPlayersWhoFolded();

        boolean everyOneElseFolded = numPlayersWhoFolded == Constants.NUM_PLAYERS - 1;
        if (everyOneElseFolded || isWinnerAtShowdown(playerId)) {
            return getPot() - getInvestment(playerId);
        } else {
            return -getInvestment(playerId);
        }
    }

    private int getNumPlayersWhoFolded() {
        int count = 0;
        for (int i=0;i< Constants.NUM_PLAYERS;i++) {
            if (hasFolded(i)) {
                count++;
            }
        }
        return count;
    }

    public int getPot() {
        int pot = 0;
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            pot += getInvestment(i);
        }
        return pot;
    }

    public boolean isWinnerAtShowdown(int playerId) {
        return (winnersAtShowdown & (1 << playerId)) > 0;
    }

    public int getInvestment(int playerId) {
        return investments[playerId];
    }

    public boolean isCurrentPlayer(int playerId) {
        return currentPlayer == playerId;
    }

    private boolean isRaiseLegal(int amount) {
        return amount >= Constants.BIG_BLIND && amount >= amountLastRaised && amount <= getStack(currentPlayer) && amount > getCallAmount();
    }

    public int getStack(int playerId) {
        return stacks[playerId];
    }

    private boolean isFoldLegal() {
        return getCallAmount() > 0;
    }

    public HoldEmGameTree takeAction(Action action) {
        try {
            HoldEmGameTree next = (HoldEmGameTree) this.clone();
            if (action.isFold()) {
                next.folded = Arrays.copyOf(folded, folded.length);
                next.fold();
            } else if (action.isCall()) {
                next.stacks = Arrays.copyOf(stacks, stacks.length);
                next.investments = Arrays.copyOf(investments, investments.length);
                next.call();
            } else {
                next.investments = Arrays.copyOf(investments, investments.length);
                next.stacks = Arrays.copyOf(stacks, stacks.length);
                next.raise(action.amount());
            }
            next.determineNextPlayer();
            next.actionTaken = action;
            next.history = new ArrayList<>(history);
            next.history.add(next);
            return next;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new IllegalStateException();
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
            for (int i = Constants.NUM_PLAYERS - 1; i > 0; i--) {
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
            int playerEndingRoundIndex = lastRaiserIndex <= 0 ? Constants.NUM_PLAYERS - 1 : lastRaiserIndex - 1;
            while (hasFolded(BETTING_ORDER_PER_ROUND[bettingRound][playerEndingRoundIndex])) {
                playerEndingRoundIndex = playerEndingRoundIndex <= 0 ? Constants.NUM_PLAYERS - 1 : playerEndingRoundIndex - 1;
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

    private void raise(int raiseAmount) {
        assert raiseAmount > 0;
        assert raiseAmount <= getStack(currentPlayer);
        int payAmount = raiseAmount + getCallAmount();
        pay(currentPlayer, payAmount);
        assert payAmount <= getStack(currentPlayer);
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            if (BETTING_ORDER_PER_ROUND[bettingRound][i] == currentPlayer) {
                lastRaiser = currentPlayer;
                break;
            }
        }
        amountLastRaised = raiseAmount - amountLastRaised;
    }

    private void pay(int playerId, int amount) {
        assert amount <= getStack(currentPlayer);
        assert amount > 0;
        subtractFromPlayerStack(playerId, amount);
        addToPlayerInvestment(playerId, amount);
    }

    private void subtractFromPlayerStack(int playerId, int amount) {
        stacks[playerId] -= amount;
        assert stacks[playerId] >= 0;
    }

    private void addToPlayerInvestment(int playerId, int amount) {
        investments[playerId] += amount;
    }

    private void addToPlayerStack(int playerId, int stack) {
        stacks[playerId] += stack;
    }

    private void fold() {
        folded[currentPlayer] = true;
    }

    private void call() {
        int payment = getCallAmount();
        if (payment > 0) {
            pay(currentPlayer, payment);
        }
    }

    private int getCallAmount() {
        int maxInvestment = -1;
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            maxInvestment = Math.max(getInvestment(p), maxInvestment);
        }
        int payment = maxInvestment - getInvestment(currentPlayer);
        return payment;
    }

    public boolean shouldUpdateRegrets() {
        return bettingRound < 2;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public List<HoldEmGameTree> history() {
        return history;
    }

    public int bettingRound() {
        return bettingRound;
    }

    public Card[] getHoleCardsFor(int player) {
        return holeCards[player];
    }

    public static Card[] getCommunityCards(HoldEmGameTree gameState) {
        return gameState.communityCards;
    }

    public Action actionTaken() {
        return actionTaken;
    }
}
