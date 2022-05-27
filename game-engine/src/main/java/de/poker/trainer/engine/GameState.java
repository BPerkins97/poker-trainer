package de.poker.trainer.engine;

import com.sun.source.tree.BreakTree;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

// TODO legality checks

/**
 * 6 max only for now
 */
public record GameState(List<Card> deck, List<Player> players, List<HoleCards> holeCards, CommunityCards communityCards,
                        ActionHistory actionHistory) {

    // TODO potentially the small blind or the big blind could have less than the blinds which would put them all in which isn't accounted for here
    public static GameState newGame(final List<Player> players, final List<Card> deck) {
        notNull(players, "This game can only be played with at least 2 players.");
        isTrue(players.size() == 6, "6 max only");

        List<Player> playersAfterBlindsArePaid = players.stream()
                .map(player -> {
                    if (player.position().equals(Position.BIG_BLIND)) {
                        return Player.pay(player, 2);
                    }
                    if (player.position().equals(Position.SMALL_BLIND)) {
                        return Player.pay(player, 1);
                    }
                    return player;
                }).toList();

        List<HoleCards> holeCards = new ArrayList<>(players.size());
        int cardCounter = 0;
        holeCards.add(new HoleCards(Position.SMALL_BLIND, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter + 1)));
        cardCounter += 2;
        holeCards.add(new HoleCards(Position.BIG_BLIND, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter + 1)));
        cardCounter += 2;
        holeCards.add(new HoleCards(Position.LOJACK, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter + 1)));
        cardCounter += 2;
        holeCards.add(new HoleCards(Position.HIJACK, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter + 1)));
        cardCounter += 2;
        holeCards.add(new HoleCards(Position.CUTOFF, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter + 1)));
        cardCounter += 2;
        holeCards.add(new HoleCards(Position.BUTTON, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter + 1)));

        return new GameState(deck, playersAfterBlindsArePaid, holeCards, null, ActionHistory.blinds());
    }

    public GameState flop() {
        return flop(this);
    }

    public static GameState flop(final GameState gameState) {
        final int firstCardIndex = gameState.players.size() * 2;
        final Card card1 = getCardAt(gameState.deck, firstCardIndex);
        final int secondCardIndex = firstCardIndex + 1;
        final Card card2 = getCardAt(gameState.deck, secondCardIndex);
        final int thirdCardIndex = secondCardIndex + 1;
        final Card card3 = getCardAt(gameState.deck, thirdCardIndex);
        final Flop flop = new Flop(card1, card2, card3);
        final CommunityCards communityCards = new CommunityCards(flop, null, null);
        return new GameState(gameState.deck, gameState.players, gameState.holeCards, communityCards, gameState.actionHistory);
    }

    public GameState turn() {
        return turn(this);
    }

    public static GameState turn(final GameState gameState) {
        final int cardIndex = gameState.players.size() * 2 + 3;
        final Card turn = getCardAt(gameState.deck, cardIndex);
        final CommunityCards communityCards = new CommunityCards(gameState.communityCards.flop(), turn, null);
        return new GameState(gameState.deck, gameState.players, gameState.holeCards, communityCards, gameState.actionHistory);
    }

    public GameState river() {
        return river(this);
    }

    public static GameState river(final GameState gameState) {
        final int cardIndex = gameState.players.size() * 2 + 4;
        final Card river = getCardAt(gameState.deck, cardIndex);
        final CommunityCards communityCards = new CommunityCards(gameState.communityCards.flop(), gameState.communityCards.turn(), river);
        return new GameState(gameState.deck, gameState.players, gameState.holeCards, communityCards, gameState.actionHistory);
    }

    public GameState takeAction(final Action action) {
        return takeAction(this, action);
    }

    public static GameState takeAction(final GameState gameState, final Action action) {
        final ActionHistory actionHistory = gameState.actionHistory == null ? new ActionHistory(null, null, null, null) : gameState.actionHistory;
        final List<Position> playersStillInHand = ActionHistory.positionsStillInHand(gameState.actionHistory());

        if (playersStillInHand.size() == 2 && action.type().equals(ActionType.FOLD)) {
            // Game End, evaluate winnings
            Position winner = playersStillInHand.stream()
                    .filter(position -> !position.equals(action.position()))
                    .findFirst()
                    .orElseThrow();
            int potSize = ActionHistory.potSize(gameState.actionHistory());
            List<Player> withWonPotAdded = gameState.players
                    .stream()
                    .map(player -> player.position().equals(winner) ? Player.winPot(player, potSize) : player)
                    .toList();
            return new GameState(gameState.deck, withWonPotAdded, gameState.holeCards(), gameState.communityCards(), gameState.actionHistory());
        }

        if (gameState.communityCards == null) {
            final List<Action> preFlopActions = actionHistory.preflopActions() == null ? new ArrayList<>() : actionHistory.preflopActions();
            if (!Position.isPlayersTurn(playersStillInHand, preFlopActions, action.position())) {
                throw new IllegalArgumentException("It's not your turn");
            }
            final List<Action> newPreFlopActions = Stream.concat(preFlopActions.stream(), Stream.of(action)).collect(Collectors.toList());
            return new GameState(gameState.deck, gameState.players, gameState.holeCards, gameState.communityCards, new ActionHistory(newPreFlopActions, null, null, null));
        }
        if (gameState.communityCards.river() != null) {
            final List<Action> riverActions = actionHistory.riverActions() == null ? new ArrayList<>() : actionHistory.riverActions();
            if (!Position.isPlayersTurn(playersStillInHand, riverActions, action.position())) {
                throw new IllegalArgumentException("It's not your turn");
            }
            final List<Action> newRiverActions = Stream.concat(riverActions.stream(), Stream.of(action)).collect(Collectors.toList());
            List<Position> positionsLeftToAct = ActionHistory.playersLeftToAct(newRiverActions, playersStillInHand);
            ActionHistory newActionHistory = new ActionHistory(actionHistory.preflopActions(), actionHistory.flopActions(), actionHistory.turnActions(), newRiverActions);
            if (CollectionUtils.isEmpty(positionsLeftToAct)) {
                // TODO showdown
                if (action.type().equals(ActionType.FOLD)) {
                    playersStillInHand.remove(action.position());
                }
                List<HoleCards> holeCardsAtShowdown = gameState.holeCards.stream()
                        .filter(holeCard -> playersStillInHand.contains(holeCard.position()))
                        .toList();
                Map<Position, Double> potWinnings = GameState.showdown(holeCardsAtShowdown, gameState.communityCards(), ActionHistory.potSize(newActionHistory));
                List<Player> playersEndStacked = gameState.players()
                        .stream()
                        .map(player -> {
                            if (potWinnings.containsKey(player.position())) {
                                return new Player(player.position(), player.stack() + potWinnings.get(player.position()));
                            } else {
                                return player;
                            }
                        })
                        .toList();
                return new GameState(gameState.deck, playersEndStacked, gameState.holeCards, gameState.communityCards, newActionHistory);
            }
            return new GameState(gameState.deck, gameState.players, gameState.holeCards, gameState.communityCards, newActionHistory);
        }
        if (gameState.communityCards.turn() != null) {
            final List<Action> turnActions = actionHistory.turnActions() == null ? new ArrayList<>() : actionHistory.turnActions();
            if (!Position.isPlayersTurn(playersStillInHand, turnActions, action.position())) {
                throw new IllegalArgumentException("It's not your turn");
            }
            final List<Action> newTurnActions = Stream.concat(turnActions.stream(), Stream.of(action)).collect(Collectors.toList());
            return new GameState(gameState.deck, gameState.players, gameState.holeCards, gameState.communityCards, new ActionHistory(actionHistory.preflopActions(), actionHistory.flopActions(), newTurnActions, null));
        }
        if (gameState.communityCards.flop() != null) {
            final List<Action> flopActions = actionHistory.flopActions() == null ? new ArrayList<>() : actionHistory.flopActions();
            if (!Position.isPlayersTurn(playersStillInHand, flopActions, action.position())) {
                throw new IllegalArgumentException("It's not your turn");
            }
            final List<Action> newFlopActions = Stream.concat(flopActions.stream(), Stream.of(action)).collect(Collectors.toList());
            return new GameState(gameState.deck, gameState.players, gameState.holeCards, gameState.communityCards, new ActionHistory(actionHistory.preflopActions(), newFlopActions, null, null));
        }
        throw new IllegalStateException("Did not expect to reach this point. Should be unreachable");
    }

    private static Map<Position, Double> showdown(List<HoleCards> holeCardsAtShowdown, CommunityCards communityCards, double potSize) {
        Map<Position, List<Card>> cards = new HashMap<>();
        holeCardsAtShowdown.stream()
                .forEach(holeCard -> cards.put(
                        holeCard.position(),
                        Arrays.asList(
                                holeCard.firstCard(),
                                holeCard.secondCard(),
                                communityCards.flop().firstCard(),
                                communityCards.flop().secondCard(),
                                communityCards.flop().thirdCard(),
                                communityCards.turn(),
                                communityCards.river()
                        )
                ));

        Map<Position, HandResult> results = new HashMap<>();

        for (Map.Entry<Position, List<Card>> hand : cards.entrySet()) {
            results.put(hand.getKey(), getHandResult(hand.getValue()));
        }

        List<Position> winners = determineWinningPositions(results);

        double winningsForEach = potSize / winners.size();

        Map<Position, Double> winnings = new HashMap<>();
        for (Position winner : winners) {
            winnings.put(winner, winningsForEach);
        }

        return winnings;
    }

    private static List<Position> determineWinningPositions(Map<Position, HandResult> results) {
        List<AbstractMap.SimpleImmutableEntry<Position, Integer>> positionsWithHandHash = results.entrySet()
                .stream()
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), HandResult.toInt(e.getValue())))
                .toList();
        int highestValue = positionsWithHandHash.stream()
                .map(AbstractMap.SimpleImmutableEntry::getValue)
                .max(Comparator.comparingInt(e -> e))
                .orElseThrow();

        return positionsWithHandHash.stream()
                .filter(e -> e.getValue().equals(highestValue))
                .map(AbstractMap.SimpleImmutableEntry::getKey)
                .toList();
    }

    private static HandResult getHandResult(List<Card> cards) {
        List<Card> finalInputCards = new ArrayList<>(cards);
        Map<CardValue, List<Card>> cardCounts = cards.stream().collect(Collectors.groupingBy(Card::value));
        Map<CardSuit, List<Card>> suitCounts = cards.stream().collect(Collectors.groupingBy(Card::suit));

        boolean isFlush = suitCounts.entrySet().stream()
                .anyMatch(entrySet -> entrySet.getValue().size() >= 5);

        List<CardValue> cardValuesInOrder = CardValue.valuesDescending();
        List<Card> straightCards = new ArrayList<>();
        int numCards = cardValuesInOrder.size();
        boolean isStraight = false;
        for (int i = 0; i < numCards - 4; i++) {
            if (cardCounts.containsKey(cardValuesInOrder.get(i))
                    && cardCounts.containsKey(cardValuesInOrder.get(i + 1))
                    && cardCounts.containsKey(cardValuesInOrder.get(i + 2))
                    && cardCounts.containsKey(cardValuesInOrder.get(i + 3))
                    && cardCounts.containsKey(cardValuesInOrder.get(i + 4))) {
                if (isFlush) {
                    List<CardSuit> possibleStraightFlushSuits = cardCounts.get(cardValuesInOrder.get(i)).stream().map(Card::suit).toList();
                    possibleStraightFlushSuits = cardCounts.get(cardValuesInOrder.get(i + 1)).stream().map(Card::suit).filter(possibleStraightFlushSuits::contains).toList();
                    possibleStraightFlushSuits = cardCounts.get(cardValuesInOrder.get(i + 2)).stream().map(Card::suit).filter(possibleStraightFlushSuits::contains).toList();
                    possibleStraightFlushSuits = cardCounts.get(cardValuesInOrder.get(i + 3)).stream().map(Card::suit).filter(possibleStraightFlushSuits::contains).toList();
                    possibleStraightFlushSuits = cardCounts.get(cardValuesInOrder.get(i + 4)).stream().map(Card::suit).filter(possibleStraightFlushSuits::contains).toList();
                    boolean isStraightFlush = possibleStraightFlushSuits.size() == 1;
                    if (isStraightFlush) {
                        final CardSuit suit = possibleStraightFlushSuits.get(0);
                        List<Card> showdownHand = cardCounts.get(cardValuesInOrder.get(i)).stream().filter(c -> c.suit().equals(suit)).toList();
                        showdownHand.addAll(cardCounts.get(cardValuesInOrder.get(i + 1)).stream().filter(c -> c.suit().equals(suit)).toList());
                        showdownHand.addAll(cardCounts.get(cardValuesInOrder.get(i + 2)).stream().filter(c -> c.suit().equals(suit)).toList());
                        showdownHand.addAll(cardCounts.get(cardValuesInOrder.get(i + 3)).stream().filter(c -> c.suit().equals(suit)).toList());
                        showdownHand.addAll(cardCounts.get(cardValuesInOrder.get(i + 4)).stream().filter(c -> c.suit().equals(suit)).toList());
                        if (cardValuesInOrder.get(i).equals(CardValue.ACE)) {
                            return new HandResult(HandClass.ROYAL_FLUSH, showdownHand);
                        }
                        return new HandResult(HandClass.STRAIGHT_FLUSH, showdownHand);
                    }
                } else {
                    straightCards.add(cardCounts.get(cardValuesInOrder.get(i)).get(0));
                    straightCards.add(cardCounts.get(cardValuesInOrder.get(i + 1)).get(0));
                    straightCards.add(cardCounts.get(cardValuesInOrder.get(i + 2)).get(0));
                    straightCards.add(cardCounts.get(cardValuesInOrder.get(i + 3)).get(0));
                    straightCards.add(cardCounts.get(cardValuesInOrder.get(i + 4)).get(0));
                    isStraight = true;
                }
            }
        }


        boolean isFourOfAKind = cardCounts.values().stream().anyMatch(c -> c.size() == 4);
        if (isFourOfAKind) {
            List<Card> showdownCards = cardCounts.values().stream().filter(c -> c.size() == 4).findAny().orElseThrow();
            finalInputCards.removeAll(showdownCards);
            showdownCards.add(sortByValueDescending(finalInputCards).get(0));
            return new HandResult(HandClass.FOUR_OF_A_KIND, showdownCards);
        }
        long numThreeOfAKind = cardCounts.entrySet().stream().filter(c -> c.getValue().size() == 3).count();
        long numPairs = cardCounts.entrySet().stream().filter(c -> c.getValue().size() == 2).count();

        if (numThreeOfAKind == 2) {
            List<Card> showdownCards = cardCounts.entrySet().stream()
                    .filter(c -> c.getValue().size() == 3)
                    .map(Map.Entry::getValue)
                    .reduce(new ArrayList<>(), (l1, l2) -> {
                        l1.addAll(l2);
                        return l1;
                    });
            showdownCards = sortByValueDescending(showdownCards).subList(0, 5);
            return new HandResult(HandClass.FULL_HOUSE, showdownCards);
        }

        if ((numThreeOfAKind == 1 && numPairs >= 1)) {
            List<Card> showdownCards = cardCounts.entrySet().stream()
                    .filter(c -> c.getValue().size() == 3)
                    .map(Map.Entry::getValue)
                    .reduce(new ArrayList<>(), (l1, l2) -> {
                        l1.addAll(l2);
                        return l1;
                    });
            List<Card> pairs = cardCounts.entrySet().stream()
                    .filter(c -> c.getValue().size() == 2)
                    .map(Map.Entry::getValue)
                    .reduce(new ArrayList<>(), (l1, l2) -> {
                        l1.addAll(l2);
                        return l1;
                    });
            showdownCards.addAll(sortByValueDescending(pairs).subList(0, 2));
            return new HandResult(HandClass.FULL_HOUSE, showdownCards);
        }

        if (isFlush) {
            CardSuit suit = suitCounts.entrySet()
                    .stream()
                    .filter(e -> e.getValue().size() >= 5)
                    .map(Map.Entry::getKey)
                    .findFirst().orElseThrow();
            List<Card> showdownCards = cards.stream()
                    .filter(c -> c.suit().equals(suit))
                    .toList();
            return new HandResult(HandClass.FLUSH, sortByValueDescending(showdownCards).subList(0, 5));
        }

        if (isStraight) {
            return new HandResult(HandClass.STRAIGHT, straightCards);
        }

        if (numThreeOfAKind == 1) {
            List<Card> showdownCards = cardCounts.entrySet().stream()
                    .filter(c -> c.getValue().size() == 3)
                    .map(Map.Entry::getValue)
                    .reduce(new ArrayList<>(), (l1, l2) -> {
                        l1.addAll(l2);
                        return l1;
                    });
            finalInputCards.removeAll(showdownCards);
            showdownCards.addAll(sortByValueDescending(finalInputCards).subList(0, 2));
            return new HandResult(HandClass.THREE_OF_A_KIND, showdownCards);
        }

        if (numPairs == 2) {
            List<Card> showdownCards = cardCounts.values().stream()
                    .filter(cardList -> cardList.size() == 2)
                    .reduce(new ArrayList<>(), (l1, l2) -> {
                        l1.addAll(l2);
                        return l1;
                    });
            showdownCards = sortByValueDescending(showdownCards);
            finalInputCards.removeAll(showdownCards);
            showdownCards.add(sortByValueDescending(finalInputCards).get(0));
            return new HandResult(HandClass.TWO_PAIR, showdownCards);
        }

        if (numPairs == 1) {
            List<Card> showdownCards = cardCounts.values().stream()
                    .filter(cardList -> cardList.size() == 2)
                    .findFirst().orElseThrow();
            finalInputCards.removeAll(showdownCards);
            showdownCards.addAll(sortByValueDescending(finalInputCards).subList(0, 3));
            return new HandResult(HandClass.PAIR, showdownCards);
        }

        List<Card> showdownCards = sortByValueDescending(cards)
                .subList(0, 5);
        return new HandResult(HandClass.HIGH_CARD, showdownCards);
    }

    private static List<Card> sortByValueDescending(List<Card> cards) {
        return cards.stream()
                .sorted(Comparator.comparingInt(c -> -c.value().descendingSortValue()))
                .toList();
    }

    private static Card getCardAt(List<Card> deck, int index) {
        return deck.get(index);
    }
}
