package de.poker.solver.map;

import de.poker.solver.game.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO https://www.nakivo.com/blog/how-to-use-remote-desktop-connection-ubuntu-linux-walkthrough/
public class ActionMap {
    private final Map<Action, Node> map = new HashMap<>();
    // Reuse this object for performance reasons

    public Strategy calculateStrategy(List<Action> actions) {
        double sum = 0;
        Strategy strategy = new Strategy(actions);
        synchronized (map) {
            for (Action action : actions) {
                Node node = getNode(action);
                int max = Math.max(0, node.getRegret());
                strategy.probabilityFor(action, max);
                sum += max;
            }
        }
        if (sum > 0) {
            strategy.normalize(sum);
        } else {
            strategy.evenlyDistributed();
        }
        return strategy;
    }

    public void addAction(Action action, Node node) {
        map.put(action, node);
    }

    private Node getNode(Action a) {
        Node node = map.get(a);
        if (Objects.isNull(node)) {
            return getOrPut(a);
        } else {
            return node;
        }
    }

    private Node getOrPut(Action a) {
        synchronized (map) {
            Node node = map.get(a);
            if (Objects.isNull(node)) {
                node = new Node(0, 0, -1);
                map.put(a, node);
            }
            return node;
        }
    }

    public void visitAction(Action action) {
        Node node = getNode(action);
        node.incrementAverageAction();
    }

    public void addRegretForAction(Action action, int regret) {
        Node node = getNode(action);
        node.addRegret(regret);
    }

    public boolean regretForActionisAboveLimit(Action action, int limit) {
        Node node = getNode(action);
        synchronized (node) {
            return node.getRegret() > limit;
        }
    }

    public Map<Action, Node> getMap() {
        return map;
    }
}
