package de.poker.solver.cfr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Node {
    private Map<Action, Double> regretSum = new HashMap<>();
    private Map<Action, Double> strategy = new HashMap<>();
    private Map<Action, Double> strategySum = new HashMap<>();

    public void addRegret(Action action, double regret) {
        if (regretSum.containsKey(action)) {
            strategy.put(action, 0.0);
            strategySum.put(action, 0.0);
            regretSum.put(action, regretSum.get(action) + regret);
        } else {
            regretSum.put(action, regret);
        }
    }

    public Map<Action, Double> getStrategy(List<Action> actions) {
        double normalizingSum = 0;
        for (Action a : actions) {
            double max = Math.max(Objects.isNull(regretSum.get(a)) ? 0 : regretSum.get(a), 0);
            strategy.put(a, max);
            normalizingSum += strategy.get(a);
        }

        for(Action a : actions) {
            if (normalizingSum > 0) {
                strategy.put(a, strategy.get(a) / normalizingSum);
            } else {
                strategy.put(a, 1.0 / actions.size());
            }
            strategySum.put(a, strategy.get(a));
        }
        return strategy;
    }
}
