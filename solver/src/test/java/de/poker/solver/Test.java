package de.poker.solver;

import de.poker.solver.game.Card;
import de.poker.solver.utility.CardInfoSetBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        int card1 = 0;
        int card2 = 1;

        List<List<Card>> flops = new ArrayList<>(20000);
        for (int i=2;i<Card.NUM_CARDS;i++) {
            for (int j=i+1;j<Card.NUM_CARDS;j++) {
                for (int k=j+1;k<Card.NUM_CARDS;k++) {
                    List<Card> sort = sort(List.of(Card.of(i), Card.of(j), Card.of(k)));
                    List<Card> norm = new ArrayList<>();
                    norm.add(Card.of(card1));
                    norm.add(Card.of(card2));
                    norm.addAll(sort);
                    List<Card> cards = CardInfoSetBuilder.toNormalizedListTest(norm);
                    cards = sort(cards.subList(2, 5));
                    norm = new ArrayList<>();
                    norm.add(cards.get(0));
                    norm.add(cards.get(1));
                    norm.addAll(cards);
                    if (!flops.contains(norm)) {
                        flops.add(norm);
                    }
                }
            }
        }
        System.out.println(flops.size());
    }

    private static List<Card> sort(List<Card> cards) {
        List<Card> cards2 = new ArrayList<>(cards);
        cards2.sort(Comparator.comparing(Card::toInt));
        cards2.sort(Comparator.reverseOrder());
        return cards2;
    }
}
