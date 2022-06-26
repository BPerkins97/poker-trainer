package de.poker.solver;

public class Player {
    public double stack = 0;
    public double investment = 0;
    private boolean hasFolded = false;
    private boolean isAllIn = false;

    public boolean isAllIn() {
        return isAllIn;
    }

    public boolean hasFolded() {
        return hasFolded;
    }

    public Player copy() {
        Player player = new Player();
        player.stack = stack;
        player.investment = investment;
        player.hasFolded = hasFolded;
        player.isAllIn = isAllIn;
        return player;
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
