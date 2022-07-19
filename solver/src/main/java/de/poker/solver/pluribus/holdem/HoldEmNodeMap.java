package de.poker.solver.pluribus.holdem;

import de.poker.solver.pluribus.Node;
import de.poker.solver.pluribus.NodeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;

// TODO this can be more efficient
public class HoldEmNodeMap implements NodeMap<HoldEmGameTree, String> {
    private Map<String, Map<String, Node>>[][] map = new HashMap[HoldEmConstants.NUM_BETTING_ROUNDS][HoldEmConstants.NUM_PLAYERS];

    public HoldEmNodeMap() {
        for (int i=0;i<HoldEmConstants.NUM_BETTING_ROUNDS;i++) {
            for (int j=0;j<HoldEmConstants.NUM_PLAYERS;j++) {
                map[i][j] = new HashMap<>();
            }
        }
    }

    @Override
    public void forEach(BiConsumer<String, Node> consumer) {
        for (int i=0;i<HoldEmConstants.NUM_BETTING_ROUNDS;i++) {
            for (int j=0;j<HoldEmConstants.NUM_PLAYERS;j++) {
                map[i][j].forEach((k, v) -> v.forEach(consumer));
            }
        }
    }

    @Override
    public void updateForCurrentPlayer(HoldEmGameTree gameTree, Node node) {
        return; // TODO solange wir in Memory arbeiten, können wir das getrost ignorieren, da wir immer mit Referenzen arbeiten
    }

    @Override
    public Node getNodeForCurrentPlayer(HoldEmGameTree gameTree) {
        Map<String, Node> tempMap = map[gameTree.bettingRound][gameTree.currentPlayer].get(gameTree.cardInfoSets[gameTree.bettingRound][gameTree.currentPlayer]);
        if (Objects.isNull(tempMap)) {
            tempMap = new HashMap<>();
            map[gameTree.bettingRound][gameTree.currentPlayer].put(gameTree.cardInfoSets[gameTree.bettingRound][gameTree.currentPlayer], tempMap);
        }

        Node node = tempMap.get(gameTree.history);
        if  (Objects.isNull(node)) {
            node = new Node(gameTree);
            tempMap.put(gameTree.history, node);
        }

        return node;
    }

    @Override
    public void discount(double discountValue) {
        forEach((key, node) -> node.discount(discountValue));
        // TODO maybe only discount touched nones
    }
}
