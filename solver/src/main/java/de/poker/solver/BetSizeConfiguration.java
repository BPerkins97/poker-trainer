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

    public static void loadFromFile(File file) throws FileNotFoundException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            BET_SIZES[0].clear();
            Arrays.stream(reader.readLine().split(";"))
                    .map(BetSize::of)
                    .forEach(size -> BET_SIZES[0].add(size));
            BET_SIZES[1].clear();
            Arrays.stream(reader.readLine().split(";"))
                    .map(BetSize::of)
                    .forEach(size -> BET_SIZES[1].add(size));
            BET_SIZES[2].clear();
            Arrays.stream(reader.readLine().split(";"))
                    .map(BetSize::of)
                    .forEach(size -> BET_SIZES[2].add(size));
            BET_SIZES[3].clear();
            Arrays.stream(reader.readLine().split(";"))
                    .map(BetSize::of)
                    .forEach(size -> BET_SIZES[3].add(size));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BetSizeConfiguration() {}
}
