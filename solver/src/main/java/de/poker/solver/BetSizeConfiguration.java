package de.poker.solver;

import de.poker.solver.game.Constants;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BetSizeConfiguration {
    public static final List<BetSize>[] BET_SIZES = new LinkedList[Constants.NUM_BETTING_ROUNDS];

    static {
        for (int i=0;i<Constants.NUM_BETTING_ROUNDS;i++) {
            BET_SIZES[i] = new LinkedList<>();
        }
    }
    private BetSizeConfiguration() {}
}
