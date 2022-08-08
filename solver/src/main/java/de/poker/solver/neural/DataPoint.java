package de.poker.solver.neural;

import de.poker.solver.game.HoldEmGameTree;

public record DataPoint(HoldEmGameTree gameState, double regret) {
}
