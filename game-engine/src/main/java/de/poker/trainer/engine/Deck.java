package de.poker.trainer.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

record Deck(List<Card> deck, List<Integer> order) {
    public static Deck fullDeck(final List<Integer> order) {
        final List<Card> cards = Arrays.stream(CardSuit.values())
                .map(suit -> Arrays.stream(CardValue.values())
                        .map(value -> new Card(value, suit))
                        .collect(Collectors.toList()))
                .reduce(new ArrayList<>(), (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                });
        return new Deck(cards, Collections.unmodifiableList(order));
    }
    public static Card getCardAt(final Deck deck, final int index) {
        return deck.deck.get(deck.order.get(index));
    }
}
