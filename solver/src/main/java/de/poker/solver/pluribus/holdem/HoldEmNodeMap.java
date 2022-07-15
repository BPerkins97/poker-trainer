package de.poker.solver.pluribus.holdem;

import de.poker.solver.pluribus.GameTree;
import de.poker.solver.pluribus.Node;
import de.poker.solver.pluribus.NodeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class HoldEmNodeMap implements NodeMap<String> {
    private Map<String, Node>[] maps = new HashMap[HoldEmConstants.NUM_PLAYERS];

    public HoldEmNodeMap() {
        for (int i=0;i<HoldEmConstants.NUM_PLAYERS;i++) {
            maps[i] = new HashMap<>();
        }
    }

    @Override
    public void forEach(BiConsumer<String, Node> consumer) {
        for (int i=0;i<HoldEmConstants.NUM_PLAYERS;i++) {
            maps[i].forEach(consumer);
        }
    }

    @Override
    public void updateForCurrentPlayer(GameTree<String> gameTree, Node node) {
        maps[gameTree.currentPlayer()].put(gameTree.asInfoSet(gameTree.currentPlayer()), node);
    }

    @Override
    public Node getNodeForCurrentPlayer(GameTree<String> state) {
        String key = state.asInfoSet(state.currentPlayer());
        if (!maps[state.currentPlayer()].containsKey(key)) {
            maps[state.currentPlayer()].put(key, new Node(state.actions()));
        }
        return maps[state.currentPlayer()].get(key);
    }

    @Override
    public void update(GameTree<String> state, int playerId, Node node) {
        String key = state.asInfoSet(playerId);
        maps[playerId].put(key, node);
    }

    @Override
    public void discount(double discountValue) {
        for (int i=0;i<HoldEmConstants.NUM_PLAYERS;i++) {
            for (Node node : maps[i].values()) {
                node.discount(discountValue);
            }
        }
        // TODO maybe only discount touched nones
    }
}
