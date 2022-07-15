package de.poker.solver.pluribus;

import java.util.function.BiConsumer;

public interface NodeMap<T extends GameTree, K> {
    void forEach(BiConsumer<K, Node> consumer);

    void updateForCurrentPlayer(T gameTree, Node node);

    Node getNodeForCurrentPlayer(T state);

    void discount(double discountValue);
}
