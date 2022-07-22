package de.poker.solver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class UserInterface {
    private final Trainer trainer;

    public UserInterface() {
        this.trainer = new Trainer();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input start to run the agent, stop to stop it again");
        String input = scanner.nextLine();
        if (input.equals("start")) {
            Thread thread = new Thread(trainer::start);
            thread.start();
        }
        input = scanner.nextLine();
        if (input.equals("stop")) {
            trainer.stop();
            System.out.println("Trainer has run " + trainer.iterations + " iterations");
        }
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
