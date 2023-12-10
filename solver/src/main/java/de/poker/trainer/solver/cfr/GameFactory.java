package de.poker.trainer.solver.cfr;

public interface GameFactory<ACTION, INFOSET> {
    Game<ACTION, INFOSET> generate();
}
