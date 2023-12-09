package de.poker.trainer.games.kuhn;

import de.poker.trainer.solver.vanillacfr.Game;

public class KuhnGame implements Game<String, String> {
    private String history = "";
    private int[] deck;

    KuhnGame() {}

    KuhnGame(int[] deck) {
        this.deck = deck;
    }


    @Override
    public boolean isGameOver() {
        return history.endsWith("bb") || history.endsWith("pp") || history.endsWith("bp");
    }

    @Override
    public double[] calculatePayoffs() {
        if (history.endsWith("pp")) {
            double[] payoffs = new double[2];
            payoffs[0] = deck[0] > deck[1] ? 1 : -1;
            payoffs[1] = deck[0] > deck[1] ? -1 : 1;
            return payoffs;
        }
        if (history.endsWith("bb")) {
            double[] payoffs = new double[2];
            payoffs[0] = deck[0] > deck[1] ? 2 : -2;
            payoffs[1] = deck[0] > deck[1] ? -2 : 2;
            return payoffs;
        }
        if (history.endsWith("bp")) {
            int currentPlayer = history.length() % 2;
            double[] payoffs = new double[2];
            payoffs[0] = currentPlayer == 0 ? 1 : -1;
            payoffs[1] = currentPlayer == 0 ? -1 : 1;
            return payoffs;
        }
        throw new IllegalStateException();
    }

    @Override
    public String getCurrentInfoSet() {
        return deck[getCurrentPlayer()] + history;
    }

    @Override
    public KuhnGame takeAction(String action) {
        KuhnGame nextGameState = new KuhnGame();
        nextGameState.history = history + action;
        nextGameState.deck = deck;
        return nextGameState;
    }

    @Override
    public int getCurrentPlayer() {
        return history.length() % 2;
    }

    @Override
    public String[] getLegalActions() {
        return new String[]{"p", "b"};
    }
}
