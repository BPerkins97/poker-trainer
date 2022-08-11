package algorithms.vanillacfr;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class InMemoryNodeMap<ACTION, INFOSET> implements NodeMap<ACTION, INFOSET> {
    private Map<INFOSET, Node<ACTION>> map = new HashMap<>();

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
    public void forEachNode(Consumer<Node<ACTION>> consumer) {
        map.forEach((k, v) -> consumer.accept(v));
    }
}
