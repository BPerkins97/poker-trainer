package de.poker.solver;

import de.poker.solver.game.Action;
import de.poker.solver.game.HoldEmGameTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ActionMap {
    private final Map<Action, Node> map = new HashMap<>();
    // Reuse this object for performance reasons
    private Strategy strategy = new Strategy();

    public Strategy calculateStrategy(List<Action> actions) {
        double sum = 0;
        strategy.reInit(actions);
        for (Action action : actions) {
            Node node = getNode(action);
            int max = Math.max(0, node.regret);
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
            node = new Node();
            map.put(a, node);
        }
        return node;
    }

    public void visitAction(Action action) {
        Node node = getNode(action);
        node.averageAction++;
    }

    public void addRegretForAction(Action action, int regret) {
        Node node = getNode(action);
        node.regret = Math.max(regret + regret, ApplicationConfiguration.MINIMUM_REGRET);
    }

    public void discount(double discountValue) {
        map.forEach((k, v) -> v.regret *= discountValue);
    }

    public boolean regretForActionisAboveLimit(Action action, int limit) {
        Node node = getNode(action);
        return node.regret > limit;
    }
}
