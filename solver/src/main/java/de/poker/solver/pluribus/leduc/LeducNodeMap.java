package de.poker.solver.pluribus.leduc;

import de.poker.solver.pluribus.Node;
import de.poker.solver.pluribus.NodeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class LeducNodeMap implements NodeMap<LeducGameTree, String> {
    private Map<String, Node>[][] map = new HashMap[LeducConstants.NUM_PLAYERS][LeducConstants.NUM_BETTING_ROUNDS];

    public LeducNodeMap() {
        for (int i=0;i<LeducConstants.NUM_PLAYERS;i++) {
            for (int j=0;j<LeducConstants.NUM_BETTING_ROUNDS;j++) {
                map[i][j] = new HashMap<>();
            }
        }
    }
    @Override
    public void forEach(BiConsumer<String, Node> consumer) {
        for (int i=0;i<LeducConstants.NUM_PLAYERS;i++) {
            for (int j=0;j<LeducConstants.NUM_BETTING_ROUNDS;j++) {
                map[i][j].forEach(consumer);
            }
        }
    }

    @Override
    public void updateForCurrentPlayer(LeducGameTree gameTree, Node node) {
        // not necessary yet
    }

    @Override
    public Node getNodeForCurrentPlayer(LeducGameTree state) {
        Node node = map[state.currentPlayer][state.bettingRound].get(state.asInfoSet(state.currentPlayer));
        if (Objects.isNull(node)) {
            node = new Node(state.actions());
            map[state.currentPlayer][state.bettingRound].put(state.asInfoSet(state.currentPlayer), node);
        }
        return node;
    }

    @Override
    public void discount(double discountValue) {
        forEach((k, v) -> v.discount(discountValue));
    }
}
