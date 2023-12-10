package de.poker.trainer.solver.cfr;

public interface NodeMap<ACTION, INFOSET> {
    Node<ACTION> getNode(Game<ACTION, INFOSET> game);
    void update(Game<ACTION, INFOSET> game, Node<ACTION> node);
}
