package de.poker.trainer.engine;

public record Player(Position position, double stack) {
    public static Player pay(Player player, double amount) {
        return new Player(player.position, player.stack - amount);
    }

    public static Player winPot(Player player, double potSize) {
        return new Player(player.position, player.stack() + potSize);
    }
}
