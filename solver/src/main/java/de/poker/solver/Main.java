package de.poker.solver;

import java.sql.SQLException;

// TODO write tests for everything
// TODO use connection pool and improve sql performance
// TODO adjust amouunt of pools and stuff based on performance
// TODO build poker app to play against
// TODO put config into database
// TODO discounting
public class Main {
    public static void main(String[] args) throws SQLException {
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(50, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(100, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[0].add(new BetSize(150, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[1].add(new BetSize(100, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[2].add(new BetSize(100, BetSize.PERCENT));
        BetSizeConfiguration.BET_SIZES[3].add(new BetSize(100, BetSize.PERCENT));
        Trainer trainer = new Trainer();
        trainer.start();
    }
}
