package de.poker.solver.tree;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameConfiguration {
    public double smallBlind;
    public double bigBlind;
    public Map<BettingRound, List<Double>> raiseAmountsPerRound = new HashMap<>();
    public Map<Position, Double> stacks = new HashMap<>();

    public static GameConfiguration defaultConfig() {
        GameConfiguration config = new GameConfiguration();
        config.stacks.put(Position.SMALL_BLIND, 100.0);
        config.stacks.put(Position.BIG_BLIND, 100.0);
        config.stacks.put(Position.LO_JACK, 100.0);
        config.stacks.put(Position.HI_JACK, 100.0);
        config.stacks.put(Position.CUT_OFF, 100.0);
        config.stacks.put(Position.BUTTON, 100.0);
        config.raiseAmountsPerRound.put(BettingRound.PRE_FLOP, Collections.emptyList());
        config.raiseAmountsPerRound.put(BettingRound.POST_FLOP, Collections.emptyList());
        config.raiseAmountsPerRound.put(BettingRound.TURN, Collections.emptyList());
        config.raiseAmountsPerRound.put(BettingRound.RIVER, Collections.emptyList());
        config.smallBlind = 0.5;
        config.bigBlind = 1;
        return config;
    }
}
