package de.poker.solver;

public class ApplicationConfiguration {
    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

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
