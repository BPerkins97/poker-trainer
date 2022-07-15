package de.poker.solver;

import de.poker.solver.cfr.Solver;
import de.poker.solver.game.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        Solver solver = new Solver();
        solver.train(1, new Random(123L));
        System.out.println("Done");
    }


    public static List<Card> generateDeck(int numCards, Random random) {
        List<Card> deck = new ArrayList<>(numCards);
        for (int i=0;i<numCards;i++) {
            Card card = Card.randomCard(random);
            while (deck.contains(card)) {
                card = Card.randomCard(random);
            }
            deck.add(card);
        }
        return deck;
    }
}
