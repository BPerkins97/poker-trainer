package de.poker.solver;

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


}
