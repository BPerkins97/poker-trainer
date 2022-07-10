package de.poker.solver.pluribus;

public interface Configuration {

    int strategyInterval();
    int pruningThreshold();
    int linearCFRThreshold();
    int discountInterval();
    int minimumRegret();
    int numPlayers();

    GameTree randomRootNode();
}
