package de.poker.solver.cfr;

public class Player {
    private double stack = 0;
    private double investment = 0;
    private boolean hasFolded = false;
    private boolean isAllIn = false;

    private HoleCards holeCards;

    public Player(double stack, HoleCards holeCards) {
        this.stack = stack;
        this.holeCards = holeCards;
    }

    public Player(Player player) {
        this.stack = player.stack;
        this.holeCards = player.holeCards;
        this.investment = player.investment;
        this.hasFolded = player.hasFolded;
        this.isAllIn = player.isAllIn;
    }

    public double stack() {
        return stack;
    }

    public HoleCards holeCards() {
        return holeCards;
    }

    public boolean isAllIn() {
        return isAllIn;
    }

    public boolean hasFolded() {
        return hasFolded;
    }

    public void fold() {
        hasFolded = true;
    }

    public void pay(double amount) {
        isAllIn = stack <= amount;
        if (isAllIn) {
            investment += stack;
            stack = 0;
        } else {
            investment += amount;
            stack -= amount;
        }
    }

    public double investment() {
        return investment;
    }
}
