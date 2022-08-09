package de.poker.solver;

import de.poker.solver.neural.NeuralNet;

import java.io.File;
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
        do {
            input = scanner.nextLine();
            if (input.equals("debug")) {
                trainer.printDebugInfo();
            }
            if (input.equals("save")) {
                try {
                    NeuralNet.save();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        } while (!input.equals("stop"));
        System.out.println("Stopped");
        trainer.stop();
    }
}
