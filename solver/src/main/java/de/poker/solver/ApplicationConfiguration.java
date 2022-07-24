package de.poker.solver;

public class ApplicationConfiguration {
    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();


    /**
     * The rate at which the cfr algorithm refines its final strategy.
     * A rate of 1 means every round, a rate of 1000 every 1000th round.
     * Unit is in iterations
     */
    public static final int STRATEGY_INTERVAL = 100;

    /**
     * Start generating the strategy at n iterations.
     */
    public static final int STRATEGY_THRESHOLD = 20_000_000;

    /**
     * After n iterations the pruning will kick in.
     */
    public static final int PRUNING_THRESHOLD = 20_000_000;

    /**
     * The minimum regret at which actions are clipped.
     * Regret can never fall below this value.
     */
    public static final int MINIMUM_REGRET = -10_000_000;
}
