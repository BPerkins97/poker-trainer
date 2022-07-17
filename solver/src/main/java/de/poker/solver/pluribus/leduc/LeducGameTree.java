package de.poker.solver.pluribus.leduc;

import de.poker.solver.pluribus.GameTree;

import java.util.ArrayList;
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
    String[] actions;

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
        setNextActions();
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
            return - investmentOf(playerId);
        }
        int opponentId = (playerId + 1) % 2;
        if (!playersStillInGame[opponentId]) {
            return investmentOf(opponentId);
        }
        long playerHand = handToLong(playerCards[playerId], communityCard);
        long opponentHand = handToLong(playerCards[opponentId], communityCard);

        if (playerHand == opponentHand) {
            return 0;
        } else if (playerHand > opponentHand) {
            return investmentOf(opponentId);
        } else {
            return - investmentOf(playerId);
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
        String infoSet = playerCards[playerId] + "|";
        if (bettingRound > 0) {
            infoSet += communityCard + "|";
        }
        return infoSet + history;
    }

    private void setNextActions() {
        ArrayList<String> actions = new ArrayList<>();
        if (isFoldLegal()) {
            actions.add("f");
        }
        actions.add("c");
        int differenceBetweenInvestments = calculateDifferenceBetweenInvestments();
        if (differenceBetweenInvestments == 0) {
            differenceBetweenInvestments++;
        } else {
            differenceBetweenInvestments *= 2;
        }
        if (differenceBetweenInvestments <= playerStacks[currentPlayer]) {
            for (int i=differenceBetweenInvestments;i<=playerStacks[currentPlayer];i++) {
                actions.add("r" + i);
            }
        }
        this.actions = actions.toArray(new String[0]);
    }

    @Override
    public int numActions() {
        return actions.length;
    }

    private int investmentOf(int playerId) {
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

        String action = actions[actionId];
        char firstLetter = action.charAt(0);
        switch (firstLetter) {
            case 'f':
                next.fold();
                break;
            case 'c':
                next.call();
                break;
            case 'r':
                int amount = Integer.parseInt(action.substring(1));
                next.raise(amount);
                break;
            default:
                throw new IllegalStateException();
        }
        next.history += actions[actionId];
        next.determineNextPlayer();
        next.setNextActions();
        return next;
    }

    private int calculateDifferenceBetweenInvestments() {
        int investmentP1 = investmentOf(PLAYER_ONE);
        int investmentP2 = investmentOf(PLAYER_TWO);
        return Math.abs(investmentP1 - investmentP2);
    }

    private void determineNextPlayer() {
        int investmentP1 = investmentOf(PLAYER_ONE);
        int investmentP2 = investmentOf(PLAYER_TWO);
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
        lastRaiser = currentPlayer;
    }

    private void call() {
        int amount = calculateDifferenceBetweenInvestments();
        pay(amount);
    }

    private void pay(int amount) {
        playerStacks = Arrays.copyOf(playerStacks, LeducConstants.NUM_PLAYERS);
        playerStacks[currentPlayer] -= amount;
    }

    private void fold() {
        playersStillInGame = Arrays.copyOf(playersStillInGame, LeducConstants.NUM_PLAYERS);
        playersStillInGame[currentPlayer] = false;
    }

    private boolean isFoldLegal() {
        return playerStacks[0] != playerStacks[1];
    }

    @Override
    public boolean shouldUpdateRegrets() {
        return true;
    }
}
