package de.poker.solver.pluribus.holdem;

import de.poker.solver.pluribus.GameTree;
import de.poker.solver.pluribus.Node;
import de.poker.solver.pluribus.NodeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class HoldEmNodeMap implements NodeMap<String> {
    Map<String, Node> map = new HashMap();

    @Override
    public void forEach(BiConsumer<String, Node> consumer) {
        map.forEach(consumer);
    }

    @Override
    public void updateForCurrentPlayer(GameTree<String> gameTree, Node node) {
        map.put(gameTree.asInfoSet(gameTree.currentPlayer()), node);
    }

    @Override
    public Node getNodeForCurrentPlayer(GameTree<String> state) {
        String key = state.asInfoSet(state.currentPlayer());
        if (!map.containsKey(key)) {
            map.put(key, new Node(state.actions()));
        }
        return map.get(key);
    }

    @Override
    public void update(GameTree<String> state, int playerId, Node node) {
        String key = state.asInfoSet(playerId);
        map.put(key, node);
    }

    @Override
    public void discount(double discountValue) {
        map.forEach((key, value) -> value.discount(discountValue)); // TODO maybe only discount touched ones
    }
}
