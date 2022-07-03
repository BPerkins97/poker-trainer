package de.poker.solver.cfr.kuhn;

public class Player {
    double stack;
    boolean hasFolded = false;

    double investment = 0;

    Player(Player player) {
        this.stack = player.stack;
        this.hasFolded = player.hasFolded;
        this.investment = player.investment;
    }

    public Player(double stack) {
        this.stack = stack;
    }

    public void fold() {
        hasFolded = true;
    }

    public void pay(double payment) {
        investment += payment;
        stack -= payment;
    }
}
