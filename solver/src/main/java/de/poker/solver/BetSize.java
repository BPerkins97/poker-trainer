package de.poker.solver;

import de.poker.solver.game.Constants;

public record BetSize(int amount, byte unit) {
    private static final byte PERCENT = 1;
    private static final byte BLINDS = 2;

    public int calculate(int pot) {
        if (unit == PERCENT) {
            return (int)((double)(pot * amount) / 100.0);
        }
        if (unit == BLINDS) {
            return amount * Constants.SMALL_BLIND;
        }
        throw new IllegalStateException();
    }

    public static BetSize of(String str) {
        if (str.endsWith("%")) {
            return new BetSize(Integer.parseInt(str.substring(0, str.length()-1)), PERCENT);
        }
        if (str.endsWith("b")) {
            return new BetSize(Integer.parseInt(str.substring(0, str.length()-1)), BLINDS);
        }
        throw new IllegalArgumentException(str);
    }
}
