package de.poker.engine.utility;

public class Assert {
    public static void assertThat(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
