package de.poker.solver.map;

import de.poker.solver.game.Action;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Strategy {
    private int numActions;
    private List<Action> actions;
    private double[] probability;
    private double[] value;
    private boolean[] explored;
    private double expectedValue;

    public Strategy(List<Action> actions) {
        numActions = actions.size();
        probability = new double[numActions];
        value = new double[numActions];
        explored = new boolean[numActions];
        this.actions = actions;
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
        return value[index] - expectedValue / probability.length;
    }

    public void value(Action action, double value) {
        int index = getIndex(action);
        this.value[index] = value * probability[index];
        expectedValue += this.value[index];
    }

    public void explored(Action action) {
        explored[getIndex(action)] = true;
    }

    public boolean hasBeenExplored(Action action) {
        return explored[getIndex(action)];
    }

    private Integer getIndex(Action action) {
        for (int i=0;i<numActions;i++) {
            if (actions.get(i).presentation().equals(action.presentation())) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }

    public double expectedValue() {
        return expectedValue;
    }

    public Action randomAction() {
        double accumulatedActionProbability = 0;
        double randomActionProbability = ThreadLocalRandom.current().nextDouble();
        for (Action action : actions) {
            accumulatedActionProbability += probability[getIndex(action)];
            if (randomActionProbability < accumulatedActionProbability) {
                return action;
            }
        }
        throw new IllegalStateException();
    }
}
