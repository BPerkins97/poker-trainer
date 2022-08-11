package algorithms.vanillacfr;

import java.util.function.Consumer;

public interface NodeMap<ACTION, INFOSET> {
    Node<ACTION> getNode(Game<ACTION, INFOSET> game);

    void update(Game<ACTION, INFOSET> game, Node<ACTION> node);

    void forEachNode(Consumer<Node<ACTION>> consumer);
}
