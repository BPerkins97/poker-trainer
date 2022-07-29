package de.poker.solver.map;

import de.poker.solver.game.Action;
import de.poker.solver.map.persistence.ActionMapInterface;

import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO https://www.nakivo.com/blog/how-to-use-remote-desktop-connection-ubuntu-linux-walkthrough/
public class ActionMap implements ActionMapInterface {
    private Map<Action, Node> map;
    // Reuse this object for performance reasons

    public Strategy calculateStrategy(List<Action> actions) {
        double sum = 0;
        Strategy strategy = new Strategy(actions);
        for (Action action : actions) {
            Node node = getNode(action);
            int max = Math.max(0, node.getRegret());
            strategy.probabilityFor(action, max);
            sum += max;
        }
        if (sum > 0) {
            strategy.normalize(sum);
        } else {
            strategy.evenlyDistributed();
        }
        return strategy;
    }

    private Node getNode(Action a) {
        Node node = map.get(a);
        if (Objects.isNull(node)) {
            node = new Node(0, (short) 0);
            map.put(a, node);
        }
        return node;
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
        return node.getRegret() > limit;
    }

    @Override
    public Map<Action, Node> getMap() {
        return map;
    }

    @Override
    public void setMap(Map<Action, Node> map) {
        this.map = map;
    }

    @Override
    public void add(ActionMapInterface persisted) {
        map.forEach((key, value) -> {
            Node node = persisted.getMap().get(key);
            if (!Objects.isNull(node)) {
                value.add(node);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionMap actionMap = (ActionMap) o;
        return Objects.equals(map, actionMap.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return "ActionMap{" +
                "map=" + map +
                '}';
    }
}
