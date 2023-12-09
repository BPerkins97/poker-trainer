package de.poker.trainer.solver.montecarlocfr;

// TODO write tests for everything
// TODO build poker app to play against
// TODO put config into database
public class Main {
    public static void main(String[] args) {
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(2, BetSize.BLINDS));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(4, BetSize.BLINDS));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(6, BetSize.BLINDS));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(25, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(50, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(75, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(100, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(150, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[1].add(new BetSize(100, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[2].add(new BetSize(50, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[2].add(new BetSize(100, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[2].add(new BetSize(150, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[3].add(new BetSize(50, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[3].add(new BetSize(100, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[3].add(new BetSize(150, BetSize.PERCENT));
        UserInterface ui = new UserInterface();
        ui.start();
    }
}
