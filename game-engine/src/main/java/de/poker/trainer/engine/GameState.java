package de.poker.trainer.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public record GameState(Deck deck, List<Player> players, List<HoleCards> holeCards, CommunityCards communityCards, ActionHistory actionHistory) {

    // TODO potentially the small blind or the big blind could have less than the blinds which would put them all in which isn't accounted for here
    public static GameState newGame(final List<Player> players, final Deck deck) {
        notNull(players, "This game can only be played with at least 2 players.");
        isTrue(players.size() >= 2, "This game can only be played with at least 2 players.");

        AtomicInteger pot = new AtomicInteger();
        List<Player> playersAfterBlindsArePaid = players.stream()
                .map(player -> {
                    if (player.position().equals(Position.BIG_BLIND)) {
                        pot.addAndGet(2);
                        return Player.pay(player, 2);
                    }
                    if (player.position().equals(Position.SMALL_BLIND)) {
                        pot.addAndGet(1);
                        return Player.pay(player, 1);
                    }
                    return player;
                })
                .collect(Collectors.toList());

        List<HoleCards> holeCards = new ArrayList<>(players.size());
        int cardCounter = 0;
        for (Player player : players) {
            Card card1 = Deck.getCardAt(deck, cardCounter);
            cardCounter++;
            Card card2 = Deck.getCardAt(deck, cardCounter);
            cardCounter++;
            holeCards.add(new HoleCards(player, card1, card2));
        }

        return new GameState(deck, Collections.unmodifiableList(playersAfterBlindsArePaid), holeCards, null, null);
    }

    public static GameState flop(final GameState gameState) {
        final int firstCardIndex = gameState.players.size() * 2;
        final Card card1 = Deck.getCardAt(gameState.deck, firstCardIndex);
        final int secondCardIndex = firstCardIndex + 1;
        final Card card2 = Deck.getCardAt(gameState.deck, secondCardIndex);
        final int thirdCardIndex = secondCardIndex + 1;
        final Card card3 = Deck.getCardAt(gameState.deck, thirdCardIndex);
        final Flop flop = new Flop(card1, card2, card3);
        final CommunityCards communityCards = new CommunityCards(flop, null, null);
        return new GameState(gameState.deck, gameState.players, gameState.holeCards, communityCards, gameState.actionHistory);
    }

    public static GameState turn(final GameState gameState) {
        final int cardIndex = gameState.players.size() * 2 + 3;
        final Card turn = Deck.getCardAt(gameState.deck, cardIndex);
        final CommunityCards communityCards = new CommunityCards(gameState.communityCards.flop(), turn, null);
        return new GameState(gameState.deck, gameState.players, gameState.holeCards, communityCards, gameState.actionHistory);
    }

    public static GameState river(final GameState gameState) {
        final int cardIndex = gameState.players.size() * 2 + 4;
        final Card river = Deck.getCardAt(gameState.deck, cardIndex);
        final CommunityCards communityCards = new CommunityCards(gameState.communityCards.flop(), gameState.communityCards.turn(), river);
        return new GameState(gameState.deck, gameState.players, gameState.holeCards, communityCards, gameState.actionHistory);
    }

    public static GameState takeAction(final GameState gameState, final Action action) {
        final ActionHistory actionHistory = gameState.actionHistory == null ? new ActionHistory(null, null, null, null) : gameState.actionHistory;

        if (gameState.communityCards.river() != null) {
            final List<Action> riverActions = actionHistory.riverActions() == null ? new ArrayList<>() : actionHistory.riverActions();
            final List<Action> newRiverActions = Stream.concat(riverActions.stream(), Stream.of(action)).collect(Collectors.toList());
            return new GameState(gameState.deck, gameState.players, gameState.holeCards, gameState.communityCards, new ActionHistory(actionHistory.preflopActions(), actionHistory.flopActions(), actionHistory.turnActions(), newRiverActions));
        }
        if (gameState.communityCards.turn() != null) {
            final List<Action> turnActions = actionHistory.turnActions() == null ? new ArrayList<>() : actionHistory.turnActions();
            final List<Action> newTurnActions = Stream.concat(turnActions.stream(), Stream.of(action)).collect(Collectors.toList());
            return new GameState(gameState.deck, gameState.players, gameState.holeCards, gameState.communityCards, new ActionHistory(actionHistory.preflopActions(), actionHistory.flopActions(), newTurnActions, null));
        }
        if (gameState.communityCards.flop() != null) {
            final List<Action> flopActions = actionHistory.flopActions() == null ? new ArrayList<>() : actionHistory.flopActions();
            final List<Action> newFlopActions = Stream.concat(flopActions.stream(), Stream.of(action)).collect(Collectors.toList());
            return new GameState(gameState.deck, gameState.players, gameState.holeCards, gameState.communityCards, new ActionHistory(actionHistory.preflopActions(), newFlopActions, null, null));
        }
        final List<Action> preFlopActions = actionHistory.preflopActions() == null ? new ArrayList<>() : actionHistory.preflopActions();
        final List<Action> newPreFlopActions = Stream.concat(preFlopActions.stream(), Stream.of(action)).collect(Collectors.toList());
        return new GameState(gameState.deck, gameState.players, gameState.holeCards, gameState.communityCards, new ActionHistory(newPreFlopActions, null, null, null));
    }
}
