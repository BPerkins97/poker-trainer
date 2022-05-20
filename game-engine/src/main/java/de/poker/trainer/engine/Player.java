package de.poker.trainer.engine;

public record Player(int id, Position position, int stack) {
    public static Player pay(Player player, int amount) {
        return new Player(player.id, player.position, player.stack - amount);
    }
}
