package de.poker.solver.pluribus;

public interface GameTree<K, T> {

    boolean isTerminalForPlayer(int playerId);

    int getPayoffForPlayer(int playerId);

    boolean isCurrentPlayer(int playerId);

    K asInfoSet(int playerId);

    int numActions();

    GameTree takeAction(int actionId);

    boolean shouldUpdateRegrets();
}
