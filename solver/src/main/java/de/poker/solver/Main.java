package de.poker.solver;

// TODO Multithreading
// TODO persist data
// TODO actions are determined based on config file which can be read at runtime
// TODO load data from checkpoint
// TODO Trainer does discounting
// TODO write tests for everything
// TODO maybe make stuff for functional
public class Main {
    public static void main(String[] args) {
        UserInterface userInterface = new UserInterface();
        userInterface.start();
    }
}
