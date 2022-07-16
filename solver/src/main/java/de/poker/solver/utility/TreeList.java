package de.poker.solver.utility;

import de.poker.solver.game.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TreeList {
    public List<TreeList> children;
    private final Card card;

    public TreeList(Card card, List<TreeList> children) {
        this.children = children;
        this.card = card;
    }
    public static List<TreeList> generate(List<Card> deck) {
        int size = deck.size();
        if (size == 1) {
            return List.of(new TreeList(deck.get(0), null));
        }
        List<TreeList> trees = new ArrayList<>(size);
        for (int i=0;i<size;i++) {
            Card item = deck.get(i);
            List<Card> reducedDeck = deck.stream()
                    .filter(c -> !c.equals(item))
                    .collect(Collectors.toList());
            List<TreeList> children = generate(reducedDeck);
            trees.add(new TreeList(item, children));
        }
        return trees;
    }

    public static List<List<Card>> combinations(List<Card> deck) {
        if (deck.size() == 1) {
            List<List<Card>> result = new ArrayList<>(1);
            result.add(deck);
            return result;
        }

        List<List<Card>> result = new ArrayList<>();
        for (int i=0;i<deck.size();i++) {
            Card item = deck.get(i);
            List<Card> reducedDeck = deck.stream()
                    .filter(c -> !c.equals(item))
                    .collect(Collectors.toList());
            List<List<Card>> children = combinations(reducedDeck);
            children.stream()
                    .peek(child -> child.add(item))
                    .forEach(result::add);
        }
        return result;

    }
}
