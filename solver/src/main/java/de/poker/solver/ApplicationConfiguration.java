package de.poker.solver;

public class ApplicationConfiguration {
    /**
     * Controls how many iterations are run before any command line info is checked.
     * The bigger the number is the slower the application will respond to user input.
     */
    public static final int RUN_ITERATIONS_AT_ONCE = 100;

    /**
     * The rate at which the cfr algorithm refines its final strategy.
     * A rate of 1 means every round, a rate of 1000 every 1000th round.
     * Unit is in iterations
     */
    public static final int STRATEGY_INTERVAL = 1;

    /**
     * Above this threshold the pruning will kick in.
     * Unit is in iterations.
     */
    public static final int PRUNING_THRESHOLD = 200;

    /**
     * Above this threshold the discounting will be stopped.
     * Unit is in iterations.
     */
    public static final int LINEAR_CFR_THRESHOLD = 400;

    /**
     * The rate at which discounting is done.
     * 1 means every round, 1000 means every 1000th round.
     * Unit is in iterations
     */
    public static final int DISCOUNT_INTERVAL = 100;

    /**
     * The minimum regret at which actions are clipped.
     * Regret can never fall below this value.
     */
    public static final int MINIMUM_REGRET = -310_000_000;
}
