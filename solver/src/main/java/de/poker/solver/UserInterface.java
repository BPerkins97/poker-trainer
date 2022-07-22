package de.poker.solver;

import java.io.File;
import java.util.Scanner;

public class UserInterface {
    private final Trainer trainer;

    public UserInterface() {
        this.trainer = new Trainer();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
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


    public void configure() {
        Scanner scanner = new Scanner(System.in);
        boolean configured = false;
        do {
            System.out.println("Save file");
            String input = scanner.nextLine();
            try {
                trainer.loadFile(new File(input));
                configured = true;
            } catch (Exception e) {
                System.out.println(e);
            }
        } while (!configured);

        configured = false;

        do {
            System.out.println("Bet size configuration file");
            String input = scanner.nextLine();
            try {
                BetSizeConfiguration.loadFromFile(new File(input));
                configured = true;
            } catch (Exception e) {
                System.out.println(e);
            }
        } while(!configured);
    }
}
