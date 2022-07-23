package de.poker.solver;

import java.sql.SQLException;
import java.util.Scanner;

public class UserInterface {
    private final Trainer trainer;
    private Scanner scanner;

    public UserInterface() throws SQLException {
        this.trainer = new Trainer();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Please input start to run the agent, stop to stop it again");
        String input = "";
        do {
            input = scanner.nextLine();
            launchCommand(input);
        } while (!input.equals("quit"));
    }

    private void launchCommand(String input) {
        switch (input) {
            case "start":
                executeStart();
                return;
            case "stop":
                executeStop();
                return;
            case "help":
                executeHelp();
                return;
            case "quit":
                return;
            default:
                System.out.println("Unknown command, type help to get info about commands");
        }
    }

    private void executeHelp() {
        System.out.println("start - Starts the training");
        System.out.println("stop - stops the training");
        System.out.println("quit - exits the program");
    }

    private void executeStop() {
        trainer.stop();
    }

    private void executeStart() {
        Thread thread = new Thread(trainer::start);
        thread.start();
    }
}
