package de.poker.solver;

public class ApplicationConfiguration {
    /**
     * The rate at which the cfr algorithm refines its final strategy.
     * A rate of 1 means every round, a rate of 1000 every 1000th round.
     * Unit is in iterations
     */
    public static final int STRATEGY_INTERVAL = 1;

    /**
     * After n minutes the pruning will kick in.
     */
    public static final int PRUNING_THRESHOLD = 200;

    /**
     * After n discount iterations the discounting will be stopped.
     */
    public static final int NUM_DISCOUNT_THRESHOLD = 40;

    /**
     * Every n minutes discounting is performed.
     */
    public static final int DISCOUNT_INTERVAL = 10;

    /**
     * The minimum regret at which actions are clipped.
     * Regret can never fall below this value.
     */
    public static final int MINIMUM_REGRET = -6_000_000;
}
