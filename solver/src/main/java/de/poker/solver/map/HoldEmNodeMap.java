package de.poker.solver.map;

import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

// TODO It probably is best to do this in a relational database
// TODO this can be more efficient
public class HoldEmNodeMap {
    Map<String, Map<String, ActionMap>>[][] map = new HashMap[Constants.NUM_BETTING_ROUNDS][Constants.NUM_PLAYERS];

    public HoldEmNodeMap() {
        for (int i = 0; i< Constants.NUM_BETTING_ROUNDS; i++) {
            for (int j = 0; j< Constants.NUM_PLAYERS; j++) {
                map[i][j] = new HashMap<>();
            }
        }
    }

    public void forEach(BiConsumer<String, ActionMap> consumer) {
        for (int i = 0; i< Constants.NUM_BETTING_ROUNDS; i++) {
            for (int j = 0; j< Constants.NUM_PLAYERS; j++) {
                map[i][j].forEach((k, v) -> v.forEach(consumer));
            }
        }
    }

    public void updateForCurrentPlayer(HoldEmGameTree gameTree, ActionMap node) {
        return; // TODO solange wir in Memory arbeiten, können wir das getrost ignorieren, da wir immer mit Referenzen arbeiten
    }

    public ActionMap getNodeForCurrentPlayer(HoldEmGameTree gameTree) {
        Map<String, ActionMap> tempMap = map[gameTree.bettingRound][gameTree.currentPlayer].get(gameTree.cardInfoSets[gameTree.bettingRound][gameTree.currentPlayer]);
        if (Objects.isNull(tempMap)) {
            tempMap = new HashMap<>();
            map[gameTree.bettingRound][gameTree.currentPlayer].put(gameTree.cardInfoSets[gameTree.bettingRound][gameTree.currentPlayer], tempMap);
        }

        ActionMap node = tempMap.get(gameTree.history());
        if  (Objects.isNull(node)) {
            node = new ActionMap();
            tempMap.put(gameTree.history(), node);
        }

        return node;
    }

    public void discount(double discountValue) {
        forEach((key, node) -> node.discount(discountValue));
        // TODO maybe only discount touched nones
    }
}
