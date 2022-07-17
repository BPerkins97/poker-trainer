package de.poker.solver.pluribus.leduc;

import de.poker.solver.pluribus.GameTree;

import java.util.Arrays;

public class LeducGameTree implements GameTree<String>, Cloneable {

    public static final int ACTION_FOLD = 0;
    public static final int ACTION_CALL = 1;
    public static final int PLAYER_ONE = 0;
    public static final int PLAYER_TWO = 1;
    int[] playerCards;
    int communityCard;
    int[] playerStacks;
    String history;
    int currentPlayer;
    boolean[] playersStillInGame;
    int bettingRound;
    int lastRaiser;

    public LeducGameTree(int[] cards) {
        playerCards = new int[LeducConstants.NUM_PLAYERS];
        playerCards[PLAYER_ONE] = cards[0];
        playerCards[PLAYER_TWO] = cards[1];
        communityCard = cards[2];
        playerStacks = new int[LeducConstants.NUM_PLAYERS];
        playerStacks[PLAYER_ONE] = LeducConstants.STARTING_STACK-1;
        playerStacks[PLAYER_TWO] = LeducConstants.STARTING_STACK-1;
        history = "";
        playersStillInGame = new boolean[LeducConstants.NUM_PLAYERS];
        Arrays.fill(playersStillInGame, true);
    }

    @Override
    public boolean isTerminalForPlayer(int playerId) {
        return someoneHasFolded() || bettingRound > 1 || everyOneIsAllIn();
    }

    private boolean everyOneIsAllIn() {
        return playerStacks[PLAYER_ONE] == 0 && playerStacks[1] == 0;
    }

    private boolean someoneHasFolded() {
        return !playersStillInGame[0] || !playersStillInGame[1];
    }

    @Override
    public int getPayoffForPlayer(int playerId) {
        if (!playersStillInGame[playerId]) {
            return - investementOf(playerId);
        }
        long playerHand = handToLong(playerCards[playerId], communityCard);
        long opponentHand = handToLong(playerCards[(playerId + 1) % 2], communityCard);

        if (playerHand > opponentHand) {
            return investementOf((playerId + 1) % 2);
        } else {
            return - (LeducConstants.STARTING_STACK - playerStacks[playerId]);
        }
    }

    private long handToLong(int playerCard, int communityCard) {
        int card1 = Math.max(playerCard, communityCard);
        int card2 = Math.min(playerCard, communityCard);
        long sum = card2;
        sum += (long) card1 * LeducConstants.NUM_CARDS;
        if (card1 == card2) {
            sum += LeducConstants.NUM_CARDS * LeducConstants.NUM_CARDS;
        }
        return sum;
    }

    @Override
    public boolean isCurrentPlayer(int playerId) {
        return currentPlayer == playerId;
    }

    @Override
    public String asInfoSet(int playerId) {
        String infoSet = "" + playerId + "|" + playerCards[playerId] + "|";
        if (bettingRound > 0) {
            infoSet += communityCard + "|";
        }
        return infoSet + history;
    }

    @Override
    public int actions() {
        int actions = 0;
        if (isFoldLegal()) {
            actions++;
        }
        actions++; // Call is always legal
        int differenceBetweenInvestments = calculateDifferenceBetweenInvestments();
        if (differenceBetweenInvestments == 0) {
            differenceBetweenInvestments++;
        }
        if (differenceBetweenInvestments < playerStacks[currentPlayer]) {
            for (int i=differenceBetweenInvestments;i<=playerStacks[currentPlayer];i++) {
                actions++;
            }
        }
        return actions;
    }

    private int investementOf(int playerId) {
        return LeducConstants.STARTING_STACK - playerStacks[playerId];
    }

    @Override
    public GameTree takeAction(int actionId) {
        LeducGameTree next;
        try {
            next = (LeducGameTree) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        if (!isFoldLegal()) {
            actionId++;
        }
        if (actionId == ACTION_FOLD) {
            next.fold();
        } else if (actionId == ACTION_CALL) {
            next.call();
        } else {
            int differenceBetweenInvestments = calculateDifferenceBetweenInvestments();
            if (differenceBetweenInvestments == 0) {
                differenceBetweenInvestments++;
            }
            int actions = ACTION_CALL;
            for (int i=differenceBetweenInvestments;i<=playerStacks[currentPlayer];i++) {
                actions++;
                if (actionId == actions) {
                    next.raise(i);
                    break;
                }
            }
        }

        next.determineNextPlayer();
        return next;
    }

    private int calculateDifferenceBetweenInvestments() {
        int investmentP1 = investementOf(PLAYER_ONE);
        int investmentP2 = investementOf(PLAYER_TWO);
        return Math.abs(investmentP1 - investmentP2);
    }

    private void determineNextPlayer() {
        int investmentP1 = investementOf(PLAYER_ONE);
        int investmentP2 = investementOf(PLAYER_TWO);
        if (investmentP1 == investmentP2) {
            currentPlayer = (currentPlayer + 1) % 2;
            if (lastRaiser == currentPlayer) {
                bettingRound++;
            }
        } else if (investmentP1 > investmentP2) {
            currentPlayer = PLAYER_TWO;
        } else {
            currentPlayer = PLAYER_ONE;
        }
    }

    private void raise(int amount) {
        pay(amount);
        history += "r" + amount;
        lastRaiser = currentPlayer;
    }

    private void call() {
        int amount = calculateDifferenceBetweenInvestments();
        pay(amount);
        history += "c";
    }

    private void pay(int amount) {
        playerStacks = Arrays.copyOf(playerStacks, LeducConstants.NUM_PLAYERS);
        playerStacks[currentPlayer] -= amount;
    }

    private void fold() {
        playersStillInGame = Arrays.copyOf(playersStillInGame, LeducConstants.NUM_PLAYERS);
        playersStillInGame[currentPlayer] = false;
        history += "f";
    }

    private boolean isFoldLegal() {
        return playerStacks[0] != playerStacks[1];
    }

    @Override
    public boolean shouldUpdateRegrets() {
        return true;
    }
}
