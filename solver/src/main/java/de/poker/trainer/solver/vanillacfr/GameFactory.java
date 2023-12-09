package de.poker.trainer.solver.vanillacfr;

public interface GameFactory<ACTION, INFOSET> {
    Game<ACTION, INFOSET> generate();
}
