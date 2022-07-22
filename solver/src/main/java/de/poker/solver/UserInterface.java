package de.poker.solver;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class UserInterface {
    private final Trainer trainer;
    private Scanner scanner;

    public UserInterface() {
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
            case "persist":
                executePersist();
                return;
            default:
                System.out.println("Unknown command, type help to get info about commands");
        }
    }

    private void executePersist() {
        System.out.println("Please specify an file to which we should write");
        boolean success = false;
        do {
            String input = scanner.nextLine();
            if (input.equals("quit")) {
                break;
            }

            File file = new File(input);
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        trainer.save(file);
                        success = true;
                    } else {
                        System.out.println("Please try again we couldnt read the fild");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    trainer.save(file);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } while (!success);
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

        configured = false;
        do {
            System.out.println("Start at iteration");
            String input = scanner.nextLine();
            try {
                int iteration = Integer.parseInt(input);
                trainer.iterations = iteration;
                configured = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while(!configured);
    }
}
