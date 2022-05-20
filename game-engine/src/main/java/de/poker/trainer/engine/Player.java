package de.poker.trainer.engine;

public record Player(Position position, int stack) {
    public static Player pay(Player player, int amount) {
        return new Player(player.position, player.stack - amount);
    }
}
