package holdem.cap.headsup.preflop;

import de.poker.solver.game.Action;
import de.poker.solver.game.Card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Game {
    private boolean[] winnersAtShowdown;
    private String[] cardInfoSets;
    private Action[] actions;

    public static Game randomGame(Random random) {
        List<Card> cards = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            Card card = Card.randomCard(random);
            while (cards.contains(card)) {
                card = Card.randomCard(random);
            }
            cards.add(card);
        }
        Game game = new Game();
        game.cardInfoSets = new String[2];
        game.cardInfoSets[0] = HandNormalizer.normalize(cards.get(0), cards.get(1));
        game.cardInfoSets[1] = HandNormalizer.normalize(cards.get(2), cards.get(3));
        int handValueOfPlayer1 = HandValueCalculator.calculateHandValue(cards.get(0), cards.get(1));
        int handValueOfPlayer2 = HandValueCalculator.calculateHandValue(cards.get(2), cards.get(3));

        game.winnersAtShowdown = new boolean[2];
        if (handValueOfPlayer1 == handValueOfPlayer2) {
            game.winnersAtShowdown[0] = true;
            game.winnersAtShowdown[1] = true;
        } else if (handValueOfPlayer1 > handValueOfPlayer2) {
            game.winnersAtShowdown[0] = true;
        } else {
            game.winnersAtShowdown[1] = true;
        }
        game.actions = new Action[0];
        return game;
    }

    public boolean isGameOver() {
        int[] stacks = new int[2];
        stacks[0] = 39;
        stacks[1] = 38;

        int currentPlayer = 0;
        boolean wasCalled = false;
        boolean wasRaised = false;

        for (Action action : actions) {
            if (action.isFold()) {
                return true;
            } else if (action.isCall()) {
                if (wasRaised || wasCalled) {
                    return true;
                }
                wasCalled = true;
            } else {
                wasRaised = true;
                stacks[currentPlayer] -= action.amount();
            }
            if (stacks[0] <= 0 && stacks[1] <= 0) {
                return true;
            }
            currentPlayer = (currentPlayer + 1) % 2;
        }

        return false;
    }

    public int[] getPayoffs() {

        int[] stacks = new int[2];
        stacks[0] = 39;
        stacks[1] = 38;

        int currentPlayer = 0;
        boolean wasCalled = false;
        boolean wasRaised = false;

        int[] payoffs = new int[2];

        for (Action action : actions) {
            if (action.isFold()) {
                payoffs[currentPlayer] = -(40 - stacks[currentPlayer]);
                int opponent = (currentPlayer + 1) % 2;
                payoffs[opponent] = (40 - stacks[opponent]) + (40 - stacks[currentPlayer]);
                return payoffs;
            } else if (action.isCall()) {
                if (wasRaised || wasCalled) {
                    if (winnersAtShowdown[0] && winnersAtShowdown[1]) {
                        payoffs[0] = 40 - stacks[0];
                        payoffs[1] = 40 - stacks[1];
                        return payoffs;
                    } else if (winnersAtShowdown[0]){
                        payoffs[0] = (40 - stacks[0]) + (40 - stacks[1]);
                        payoffs[1] = -(40 - stacks[1]);
                        return payoffs;
                    } else {
                        payoffs[0] = -(40 - stacks[0]);
                        payoffs[1] = (40 - stacks[0]) + (40 - stacks[1]);
                        return payoffs;
                    }
                }
                wasCalled = true;
            } else {
                wasRaised = true;
                stacks[currentPlayer] -= action.amount();
            }
            if (stacks[0] <= 0 && stacks[1] <= 0) {
                if (winnersAtShowdown[0] && winnersAtShowdown[1]) {
                    payoffs[0] = 40;
                    payoffs[1] = 40;
                    return payoffs;
                } else if (winnersAtShowdown[0]){
                    payoffs[0] = 80;
                    payoffs[1] = -40;
                    return payoffs;
                } else {
                    payoffs[0] = -40;
                    payoffs[1] = 80;
                    return payoffs;
                }
            }
        }
        throw new IllegalStateException();
    }

    public String getInfoSetOfGame() {
        return cardInfoSets[currentPlayer()] + Arrays.stream(actions).map(Action::toString).collect(Collectors.joining());
    }

    public Action[] getLegalActions() {
        List<Action> legalActions = new ArrayList<>();
        if (actions.length == 0 || actions[actions.length-1].isRaise()) {
            legalActions.add(Action.fold());
        }
        legalActions.add(Action.call());
        int minRaise = 2;
        if (actions.length > 0 && actions[actions.length-1].isRaise()) {
            minRaise = actions[actions.length-1].amount();
        }

        int[] stacks = new int[2];
        stacks[0] = 39;
        stacks[1] = 38;

        int previousRaise = 1;
        int currentPlayer = 0;
        for (Action action : actions) {
            if (action.isRaise()) {
                stacks[currentPlayer] -= previousRaise + action.amount();
                previousRaise = action.amount();
            }
            currentPlayer = (currentPlayer + 1) % 2;
        }
        for (int i=minRaise;i<stacks[currentPlayer()];i+=2) {
            legalActions.add(Action.raise(i));
        }
        return legalActions.toArray(new Action[0]);
    }

    public Game takeAction(Action action) {
        Game nextGame = new Game();
        nextGame.cardInfoSets = cardInfoSets;
        nextGame.winnersAtShowdown = winnersAtShowdown;
        nextGame.actions = Arrays.copyOf(actions, actions.length+1);
        nextGame.actions[actions.length] = action;
        return nextGame;
    }

    public int currentPlayer() {
        return actions.length % 2;
    }
}
