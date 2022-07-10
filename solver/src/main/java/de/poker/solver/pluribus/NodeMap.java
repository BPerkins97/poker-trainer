package de.poker.solver.pluribus;

import java.util.function.BiConsumer;

public interface NodeMap<K> {
    void forEach(BiConsumer<K, Node> consumer);

    void updateForCurrentPlayer(GameTree<K> gameTree, Node node);

    Node getNodeForCurrentPlayer(GameTree<K> state);

    void update(GameTree<K> state, int traversingPlayerId, Node node);

    void discount(double discountValue);
}
