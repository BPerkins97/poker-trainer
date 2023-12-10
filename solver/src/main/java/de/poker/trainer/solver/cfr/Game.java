package de.poker.trainer.solver.cfr;

public interface Game<ACTION, INFOSET> {
    boolean isGameOver();
    double[] calculatePayoffs();

    INFOSET getCurrentInfoSet();

    <T extends Game<ACTION, INFOSET>> T takeAction(ACTION action);

    int getCurrentPlayer();

    ACTION[] getLegalActions();
}