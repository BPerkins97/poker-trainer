package de.poker.solver.cfr;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Solver {
    private NodeDAO nodeDAO = new InMemoryNodeDAO();
    private int counter = 0;

    public static void main(String[] args) {
        int iterations = 100;
        Map<Position, Double> values = new HashMap<>();
        List<List<Card>> cards = buildDecks(17, iterations);
        Solver solver = new Solver();
        for (int i=0;i<iterations;i++) {
            GameConfiguration gameConfiguration = GameConfiguration.defaultConfig().withCards(cards.get(i));
            GameState gameState = new GameState(gameConfiguration);
            Map<Position, Double> cfr = solver.cfr(gameState);
            for (Map.Entry<Position, Double> e : cfr.entrySet()) {
                if (!values.containsKey(e.getKey())) {
                    values.put(e.getKey(), 0.0);
                }
                values.put(e.getKey(), values.get(e.getKey()) + (e.getValue() / iterations));
            }
        }
        System.out.println(solver.counter);
        System.out.println(values);
    }

    private static List<List<Card>> buildDecks(int numOfCards, int numDecks) {
        List<List<Card>> result = new ArrayList<>(numDecks);

        for (int i=0;i<numDecks;i++) {
            List<Card> cards = new ArrayList<>(numOfCards);
            for (int j=0;j<numOfCards;j++) {

                Card card;
                do {
                    int nextCard = ThreadLocalRandom.current().nextInt(4, 56);
                    card = Card.of(nextCard);
                } while(cards.contains(card));
                cards.add(card);
            }
            result.add(cards);
        }
        return result;
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
        counter++;
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
