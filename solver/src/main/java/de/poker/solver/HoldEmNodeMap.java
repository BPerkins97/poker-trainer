package de.poker.solver;

import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

// TODO this can be more efficient
public class HoldEmNodeMap {
    private Map<String, Map<String, Node>>[][] map = new HashMap[Constants.NUM_BETTING_ROUNDS][Constants.NUM_PLAYERS];

    public HoldEmNodeMap() {
        for (int i = 0; i< Constants.NUM_BETTING_ROUNDS; i++) {
            for (int j = 0; j< Constants.NUM_PLAYERS; j++) {
                map[i][j] = new HashMap<>();
            }
        }
    }

    public void forEach(BiConsumer<String, Node> consumer) {
        for (int i = 0; i< Constants.NUM_BETTING_ROUNDS; i++) {
            for (int j = 0; j< Constants.NUM_PLAYERS; j++) {
                map[i][j].forEach((k, v) -> v.forEach(consumer));
            }
        }
    }

    public void updateForCurrentPlayer(HoldEmGameTree gameTree, Node node) {
        return; // TODO solange wir in Memory arbeiten, können wir das getrost ignorieren, da wir immer mit Referenzen arbeiten
    }

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

    public void discount(double discountValue) {
        forEach((key, node) -> node.discount(discountValue));
        // TODO maybe only discount touched nones
    }
}
