package de.poker.solver.cfr;

import de.poker.solver.tree.GameConfiguration;
import de.poker.solver.tree.Position;

import java.util.*;

public class Solver {
    private NodeDAO nodeDAO = new InMemoryNodeDAO();

    public static void main(String[] args) {
        List<Card> cards = count17Cards();
        GameConfiguration gameConfiguration = GameConfiguration.defaultConfig().withCards(cards);
        GameState gameState = new GameState(gameConfiguration);
        Solver solver = new Solver();
        Map<Position, Double> cfr = solver.cfr(gameState);
        System.out.println(cfr);
    }

    private static List<Card> count17Cards() {
        List<Card> cards = new ArrayList<>(17);
        int cardCounter = 0;
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Value value : Card.Value.values()) {
                cards.add(new Card(value, suit));
                cardCounter++;
                if (cardCounter == 17) {
                    return cards;
                }
            }
        }
        return Collections.emptyList();
    }

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
        nodeDAO.persist(infoSet, node);
        return utilitySums;
    }
}
