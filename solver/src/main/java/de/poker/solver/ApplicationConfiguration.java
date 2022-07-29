package de.poker.solver;

public class ApplicationConfiguration {
    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    /**
     * The minimum regret at which actions are clipped.
     * Regret can never fall below this value.
     */
    public static final int MINIMUM_REGRET = -10_000_000;
}
