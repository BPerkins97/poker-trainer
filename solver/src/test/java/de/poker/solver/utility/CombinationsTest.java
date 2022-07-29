package de.poker.solver.utility;

import de.poker.solver.game.Card;

import java.util.*;

public class CombinationsTest {

    public static void main(String[] args) {
        Set<List<Card>> permutations = new HashSet<>();
        for (int i=0;i< Card.NUM_CARDS;i++) {
            for (int j=i+1;j<Card.NUM_CARDS;j++) {
                for (int k=j+1;k<Card.NUM_CARDS;k++) {
                    for (int l=k+1;l<Card.NUM_CARDS;l++) {
                        for (int m=l+1;m<Card.NUM_CARDS;m++) {
                            for (int n=m+1;n<Card.NUM_CARDS;n++) {
                                for (int o=n+1;o<Card.NUM_CARDS;o++) {
                                    List<Card> of = Arrays.asList(Card.of(i), Card.of(j), Card.of(k), Card.of(l), Card.of(m), Card.of(n), Card.of(o));
                                    List<List<Card>> permutations1 = permutations(of);
                                    for (List<Card> permutation : permutations1) {
                                        List<Card> normalizedCards = CardInfoSetBuilder.toNormalizedList(permutation);
                                        permutations.add(normalizedCards);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println(permutations.size());
    }

    private static List<List<Card>> permutations(List<Card> cards) {
        List<List<Card>> permutations = new LinkedList<>();
        if (cards.size() == 1) {
            List<Card> list = new LinkedList<>();
            list.add(cards.get(0));
            permutations.add(list);
            return permutations;
        }

        for (int i=0;i<cards.size();i++) {
            Card currentCard = cards.get(i);
            List<Card> filteredDeck = cards.stream()
                    .filter(card -> !card.equals(currentCard))
                    .toList();
            List<List<Card>> permutations1 = permutations(filteredDeck);
            for (List<Card> permutation : permutations1) {
                permutation.add(currentCard);
                permutations.add(permutation);
            }
        }
        return permutations;
    }
}
