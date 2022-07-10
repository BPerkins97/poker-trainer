package de.poker.solver.pluribus;

public record Configuration(
        int strategyInterval,
        int pruningThreshold,
        int linearCFRThreshold,
        int discountInterval,
        int minimumRegret) {
}
