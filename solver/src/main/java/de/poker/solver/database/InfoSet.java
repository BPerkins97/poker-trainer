package de.poker.solver.database;

public record InfoSet(
        byte player,
        long cards,
        String history
        ) {
}