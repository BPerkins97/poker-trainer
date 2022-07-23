package de.poker.solver;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        UserInterface userInterface = new UserInterface();
        userInterface.start();
    }
}
