package de.poker.solver.pluribus;

public interface GameTree<K> {

    boolean isTerminalForPlayer(int playerId);

    int getPayoffForPlayer(int playerId);

    boolean isCurrentPlayer(int playerId);

    K asInfoSet(int playerId);

    int actions();

    GameTree takeAction(int actionId);

    boolean shouldUpdateRegrets();

    int currentPlayer();
}
