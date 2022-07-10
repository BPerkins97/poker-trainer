package de.poker.solver.pluribus;

public interface GameTree {

    boolean isTerminalForPlayer(int playerId);

    int getPayoff(int playerId);

    boolean isCurrentPlayer(int playerId);

    InfoSet asInfoSet();

    int actions();

    GameTree takeAction(int actionId);

    boolean shouldUpdateRegrets();

    int numPlayers();
}
