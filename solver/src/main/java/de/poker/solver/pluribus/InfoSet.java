package de.poker.solver.pluribus;

public interface InfoSet {
    boolean isCurrentPlayer(int currentPlayer);

    int actions();

    boolean shouldCalculateBluePrintStrategy();
}
