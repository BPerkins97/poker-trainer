package de.poker.solver.game;

import de.poker.solver.BetSizeConfiguration;
import de.poker.solver.database.InfoSet;
import de.poker.solver.utility.CardInfoSetBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class HoldEmGameTree implements Cloneable {
    private static final int NUM_BITS_FOR_STACK = 8;
    private static final long STACK_BITMASK = 0xff;

    private static final Action[] ACTIONS = new Action[102];
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
    public static final int BETTING_ROUND_PRE_FLOP = 0;
    public static final int BETTING_ROUND_FLOP = 1;
    public static final int BETTING_ROUND_TURN = 2;
    public static final int BETTING_ROUND_RIVER = 3;
    public static final int ACTION_ID_FOLD = 100;
    public static final int ACTION_ID_CALL = 101;

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
        for (int i = Constants.BIG_BLIND; i <= Constants.STARTING_STACK_SIZE; i += Constants.BIG_BLIND) {
            ACTIONS[i / Constants.BIG_BLIND - 1] = Action.raise(i);
        }
        ACTIONS[100] = Action.fold();
        ACTIONS[101] = Action.call();
    }

    private String history = "";
    public long[][] cardInfoSets;
    public byte currentPlayer;
    public byte playersWhoFolded;
    public long playersStacks;
    public long playerInvestments;
    public byte winnersAtShowdown;
    public boolean isGameOver;
    public byte bettingRound;
    public int lastRaiser;
    public int amountLastRaised;
    public List<Action> nextActions;


    public HoldEmGameTree(Card[] deck) {
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            addToPlayerStack(i, Constants.STARTING_STACK_SIZE);
        }

        pay(POSITION_SMALL_BLIND, Constants.SMALL_BLIND);
        pay(POSITION_BIG_BLIND, Constants.BIG_BLIND);
        currentPlayer = BETTING_ORDER_PER_ROUND[0][0];
        cardInfoSets = new long[Constants.NUM_BETTING_ROUNDS][Constants.NUM_PLAYERS];
        lastRaiser = -1;
        long[] playersHands = new long[Constants.NUM_PLAYERS];
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            int startIndex = 2 * i;

            CardInfoSetBuilder infoSetBuilder = new CardInfoSetBuilder();
            infoSetBuilder.appendHoleCards(deck[startIndex], deck[startIndex + 1]);
            cardInfoSets[0][i] = infoSetBuilder.toLong();
            infoSetBuilder.appendFlop(deck[FLOP_CARD1], deck[FLOP_CARD2], deck[FLOP_CARD3]);
            cardInfoSets[1][i] = infoSetBuilder.toLong();
            infoSetBuilder.appendCard(deck[TURN_CARD]);
            cardInfoSets[2][i] = infoSetBuilder.toLong();
            infoSetBuilder.appendCard(deck[RIVER_CARD]);
            cardInfoSets[3][i] = infoSetBuilder.toLong();
            playersHands[i] = HandEvaluator.of(infoSetBuilder.cards());
        }

        long bestHandValue = Long.MIN_VALUE;
        for (int p = 0; p < Constants.NUM_PLAYERS; p++) {
            bestHandValue = Math.max(bestHandValue, playersHands[p]);
        }

        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            if (bestHandValue == playersHands[i]) {
                addWinnerAtShowdown(i);
            }
        }

        setNextActions();
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

    public static HoldEmGameTree getRandomRootState(Random random) {
        int numCards = Constants.NUM_PLAYERS * 2 + 5;
        Card[] deck = new Card[numCards];
        for (int i = 0; i < numCards; i++) {
            Card card;
            do {
                card = Card.randomCard(random);
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
        return (playersWhoFolded & (1 << playerId)) > 0;
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
        byte folded = playersWhoFolded;
        while (folded > 0) {
            count += folded & 1;
            folded >>= 1;
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
        return (int) ((STACK_BITMASK << (NUM_BITS_FOR_STACK * playerId) & playerInvestments) >> (NUM_BITS_FOR_STACK * playerId));
    }

    public boolean isCurrentPlayer(int playerId) {
        return currentPlayer == playerId;
    }

    private boolean isRaiseLegal(int amount) {
        return amount >= Constants.BIG_BLIND && amount >= amountLastRaised && amount <= getStack(currentPlayer) && amount > getCallAmount();
    }

    private int getStack(int playerId) {
        return (int) ((STACK_BITMASK << (NUM_BITS_FOR_STACK * playerId) & playersStacks) >> (NUM_BITS_FOR_STACK * playerId));
    }

    private boolean isFoldLegal() {
        return getCallAmount() > 0;
    }

    public HoldEmGameTree takeAction(Action action) {
        try {
            HoldEmGameTree next = (HoldEmGameTree) this.clone();
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
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    private void setNextActions() {
        this.nextActions = new ArrayList<>();
        switch (bettingRound) {
            case BETTING_ROUND_PRE_FLOP:
                getPreFlopActions();
                break;
            case BETTING_ROUND_FLOP:
                getFlopActions();
                break;
            case BETTING_ROUND_TURN:
                getTurnActions();
                break;
            case BETTING_ROUND_RIVER:
                getRiverActions();
                break;
            default:
                throw new IllegalStateException();
        }
    }

    private void getRiverActions() {
        if (isFoldLegal()) {
            nextActions.add(Action.fold());
        }
        nextActions.add(Action.call());
        BetSizeConfiguration.BET_SIZES[BETTING_ROUND_RIVER].forEach(betSize -> addRaiseIfLegal(betSize.calculate(getPot())));
        addRaiseIfLegal(getStack(currentPlayer));
    }

    private void getTurnActions() {
        if (isFoldLegal()) {
            nextActions.add(Action.fold());
        }
        nextActions.add(Action.call());
        BetSizeConfiguration.BET_SIZES[BETTING_ROUND_TURN].forEach(betSize -> addRaiseIfLegal(betSize.calculate(getPot())));
        addRaiseIfLegal(getStack(currentPlayer));
    }

    private void getFlopActions() {
        if (isFoldLegal()) {
            nextActions.add(Action.fold());
        }
        nextActions.add(Action.call());
        BetSizeConfiguration.BET_SIZES[BETTING_ROUND_FLOP].forEach(betSize -> addRaiseIfLegal(betSize.calculate(getPot())));
        addRaiseIfLegal(getStack(currentPlayer));
    }

    private void getPreFlopActions() {
        if (isFoldLegal()) {
            nextActions.add(Action.fold());
        }
        nextActions.add(Action.call());
        BetSizeConfiguration.BET_SIZES[BETTING_ROUND_PRE_FLOP].forEach(betSize -> addRaiseIfLegal(betSize.calculate(getPot())));
        addRaiseIfLegal(getStack(currentPlayer));
    }

    private void addRaiseIfLegal(int raiseAmount) {
        if (!isRaiseLegal(raiseAmount)) {
            return;
        }
        Action raise = Action.raise(raiseAmount);
        if (!nextActions.contains(raise)) {
            nextActions.add(raise);
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

    private void raise(int amount) {
        assert amount > 0;
        assert amount <= getStack(currentPlayer);
        pay(currentPlayer, amount);
        for (int i = 0; i < Constants.NUM_PLAYERS; i++) {
            if (BETTING_ORDER_PER_ROUND[bettingRound][i] == currentPlayer) {
                lastRaiser = currentPlayer;
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
    }

    private void subtractFromPlayerStack(int playerId, int amount) {
        playersStacks -= (long) amount << (NUM_BITS_FOR_STACK * playerId);
    }

    private void addToPlayerInvestment(int playerId, int amount) {
        playerInvestments += (long) amount << (NUM_BITS_FOR_STACK * playerId);
    }

    private void addToPlayerStack(int playerId, int stack) {
        playersStacks += (long) stack << (NUM_BITS_FOR_STACK * playerId);
    }

    private void fold() {
        playersWhoFolded = (byte) (playersWhoFolded | (1 << currentPlayer));
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

    public String history() {
        return history;
    }

    public int bettingRound() {
        return bettingRound;
    }

    public List<Action> actions() {
        return nextActions;
    }

    public long cardInfoSet(int bettingRound, int player) {
        return cardInfoSets[bettingRound][player];
    }

    public InfoSet toInfoSet() {
        return new InfoSet(currentPlayer, cardInfoSet(bettingRound, currentPlayer), history);
        // TODO
    }
}
