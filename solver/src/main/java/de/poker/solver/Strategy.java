package de.poker.solver;

import de.poker.solver.game.Action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Strategy {
    private int numActions;
    private Map<Action, Integer> actions;
    private double[] probability;
    private double[] value;
    private boolean[] explored;
    private double expectedValue;

    public Strategy(List<Action> actions) {
        numActions = actions.size();
        probability = new double[numActions];
        value = new double[numActions];
        explored = new boolean[numActions];
        this.actions = new HashMap<>();
        for (int i=0;i<numActions;i++) {
            this.actions.put(actions.get(i), i);
        }
    }

    public void probabilityFor(Action action, double probability) {
        this.probability[getIndex(action)] = probability;
    }

    public void normalize(double sum) {
        for (int i=0;i<numActions;i++) {
            probability[i] /= sum;
        }
    }

    public void evenlyDistributed() {
        for (int i=0;i<numActions;i++) {
            probability[i] = 1.0 / numActions;
        }
    }

    public double value(Action action) {
        int index = getIndex(action);
        return probability[index] * value[index];
    }

    public double normalizedValue(Action action) {
        int index = getIndex(action);
        return probability[index] * value[index] - expectedValue;
    }

    public void value(Action action, double value) {
        this.value[getIndex(action)] = value;
        expectedValue += value;
    }

    public void explored(Action action) {
        explored[getIndex(action)] = true;
    }

    public boolean hasBeenExplored(Action action) {
        return explored[getIndex(action)];
    }

    private Integer getIndex(Action action) {
        return actions.get(action);
    }

    public double expectedValue() {
        return expectedValue;
    }

    public Action randomAction() {
        double accumulatedActionProbability = 0;
        double randomActionProbability = ThreadLocalRandom.current().nextDouble();
        for (Map.Entry<Action, Integer> action : actions.entrySet()) {
            accumulatedActionProbability += probability[action.getValue()];
            if (randomActionProbability < accumulatedActionProbability) {
                return action.getKey();
            }
        }
        throw new IllegalStateException();
    }
}
