package de.poker.trainer.solver.cfr;

import java.util.HashMap;
import java.util.Map;

public class InMemoryNodeMap<ACTION, INFOSET> implements NodeMap<ACTION, INFOSET> {
    private final Map<INFOSET, Node<ACTION>> map = new HashMap<>();

    @Override
    public Node<ACTION> getNode(Game<ACTION, INFOSET> game) {
        INFOSET infoSet = game.getCurrentInfoSet();
        if (!map.containsKey(infoSet)) {
            Node<ACTION> node = new Node<>(game.getLegalActions());
            map.put(infoSet, node);
        }
        return map.get(infoSet);
    }

    @Override
    public void update(Game<ACTION, INFOSET> game, Node<ACTION> node) {
        map.put(game.getCurrentInfoSet(), node);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        map.forEach((k, v) -> sb.append(String.format("%10s: \t%s\n", k, v)));
        return sb.toString();
    }
}
