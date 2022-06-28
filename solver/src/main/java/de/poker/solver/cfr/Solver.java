package de.poker.solver.cfr;

import de.poker.solver.tree.Position;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solver {
    private NodeDAO nodeDAO;


    public Map<Position, Double> cfr(GameState gameState) {
        if (gameState.isGameOver()) {
            return gameState.handleGameOver();
        }

        String infoSet = gameState.toInfoSetFor(gameState.currentPlayer());
        Node node = nodeDAO.findByInfoSet(infoSet)
                .orElseGet(Node::new);

        List<Action> nextActions = gameState.nextActions();
        Map<Action, Double> strategy = node.getStrategy(nextActions);

        Map<Action, Double> actionUtility = new HashMap<>();
        Set<Action> actions = strategy.keySet();
        Map<Position, Double> utilitySums = new HashMap<>();
        for (Action action : actions) {
            GameState nextState = gameState.takeAction(action);
            Map<Position, Double> utility = cfr(nextState);
            utility.forEach((key, value) -> {
                double pUtility = value * strategy.get(action);
                if (utilitySums.containsKey(key)) {
                    utilitySums.put(key, utilitySums.get(key) + pUtility);
                } else {
                    utilitySums.put(key, pUtility);
                }
            });
            actionUtility.put(action, utility.get(gameState.currentPlayer()));
        }

        for (Action action : actions) {
            node.addRegret(action, actionUtility.get(action) - utilitySums.get(gameState.currentPlayer()));
        }
        return utilitySums;
    }
}
