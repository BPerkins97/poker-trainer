package de.poker.solver.pluribus.kuhn;

import de.poker.solver.pluribus.Node;
import de.poker.solver.pluribus.NodeMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class KuhnNodeMap implements NodeMap<KuhnGameTree, String> {
    private Map<String, Node> map = new HashMap<>();

    @Override
    public void forEach(BiConsumer<String, Node> consumer) {
        map.forEach(consumer);
    }

    @Override
    public void updateForCurrentPlayer(KuhnGameTree gameTree, Node node) {
        // nothing to do because everything is in memory
    }

    @Override
    public Node getNodeForCurrentPlayer(KuhnGameTree state) {
        String infoSet = state.asInfoSet(state.currentPlayer());
        Node node = map.get(infoSet);
        if (Objects.isNull(node)) {
            node = new Node(2);
            map.put(infoSet, node);
        }
        return node;
    }

    @Override
    public void discount(double discountValue) {
        forEach((k, v) -> v.discount(discountValue));
    }
}
