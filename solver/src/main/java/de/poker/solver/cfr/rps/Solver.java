package de.poker.solver.cfr.rps;

import java.util.concurrent.ThreadLocalRandom;

public class Solver {
    public static final int NUM_ACTIONS = 3;
    private static final int[][] ACTION_UTILITY = new int[NUM_ACTIONS][];
    private static final int ROCK = 0, PAPER = 1, SCISSORS = 2;

    static {
        ACTION_UTILITY[ROCK] = new int[NUM_ACTIONS];
        ACTION_UTILITY[ROCK][ROCK] = 0;
        ACTION_UTILITY[ROCK][PAPER] = -1;
        ACTION_UTILITY[ROCK][SCISSORS] = 1;

        ACTION_UTILITY[PAPER] = new int[NUM_ACTIONS];
        ACTION_UTILITY[PAPER][ROCK] = 1;
        ACTION_UTILITY[PAPER][PAPER] = 0;
        ACTION_UTILITY[PAPER][SCISSORS] = -1;

        ACTION_UTILITY[SCISSORS] = new int[NUM_ACTIONS];
        ACTION_UTILITY[SCISSORS][ROCK] = -1;
        ACTION_UTILITY[SCISSORS][PAPER] = 1;
        ACTION_UTILITY[SCISSORS][SCISSORS] = 0;
    }

    private Node myStrategy = new Node();
    private Node opponentStrategy = new Node();

    public static void main(String[] args) {
        Solver solver = new Solver();
        solver.train(10000);
        double[] myTargetPolicy = solver.myStrategy.getAverageStrategy();
        double[] opponentTargetPolicy = solver.opponentStrategy.getAverageStrategy();
        System.out.println("My Policy");
        System.out.println("ROCK " + myTargetPolicy[ROCK]);
        System.out.println("PAPER " + myTargetPolicy[PAPER]);
        System.out.println("SCISSORS " + myTargetPolicy[SCISSORS]);
        System.out.println("Opponent Policy");
        System.out.println("ROCK " + opponentTargetPolicy[ROCK]);
        System.out.println("PAPER " + opponentTargetPolicy[PAPER]);
        System.out.println("SCISSORS " + opponentTargetPolicy[SCISSORS]);
    }

    public int getAction(double[] strategy) {
        double rand = ThreadLocalRandom.current().nextDouble();
        double randSum = 0;
        for (int i=0;i<NUM_ACTIONS;i++) {
            randSum += strategy[i];
            if (rand <= randSum) {
                return i;
            }
        }
        throw new IllegalStateException();
    }

    public int getReward(int myAction, int opponentAction) {
        return ACTION_UTILITY[myAction][opponentAction];
    }

    public void train(int iterations) {
        for (int i=0;i<iterations;i++) {
            double[] myStrategy = this.myStrategy.getStrategy();
            double[] opponentStrategy = this.opponentStrategy.getStrategy();
            for (int a=0;a<NUM_ACTIONS;a++) {
                this.myStrategy.strategySum[a] += myStrategy[a];
                this.opponentStrategy.strategySum[a] += opponentStrategy[a];
            }

            int myAction = getAction(myStrategy);
            int opponentAction = getAction(opponentStrategy);

            int myReward = getReward(myAction, opponentAction);
            int opponentReward = getReward(opponentAction, myAction);

            for (int a=0;a<NUM_ACTIONS;a++) {
                int myRegret = getReward(a, opponentAction) - myReward;
                int opponentRegret = getReward(a, opponentAction) - opponentReward;

                this.myStrategy.regretSum[a] += myRegret;
                this.opponentStrategy.regretSum[a] += opponentRegret;
            }
        }
    }
}
