package de.poker.trainer.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

// TODO legality checks
/**
 * 6 max only for now
 */
public record GameState(List<Card> deck, List<Player> players, List<HoleCards> holeCards, CommunityCards communityCards, ActionHistory actionHistory) {

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
                })
                .collect(Collectors.toList());

        List<HoleCards> holeCards = new ArrayList<>(players.size());
        int cardCounter = 0;
        holeCards.add(new HoleCards(Position.SMALL_BLIND, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter+1)));
        cardCounter += 2;
        holeCards.add(new HoleCards(Position.BIG_BLIND, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter+1)));
        cardCounter += 2;
        holeCards.add(new HoleCards(Position.LOJACK, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter+1)));
        cardCounter += 2;
        holeCards.add(new HoleCards(Position.HIJACK, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter+1)));
        cardCounter += 2;
        holeCards.add(new HoleCards(Position.CUTOFF, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter+1)));
        cardCounter += 2;
        holeCards.add(new HoleCards(Position.BUTTON, getCardAt(deck, cardCounter), getCardAt(deck, cardCounter+1)));

        return new GameState(deck, Collections.unmodifiableList(playersAfterBlindsArePaid), holeCards, null, null);
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

    public static GameState turn(final GameState gameState) {
        final int cardIndex = gameState.players.size() * 2 + 3;
        final Card turn = getCardAt(gameState.deck, cardIndex);
        final CommunityCards communityCards = new CommunityCards(gameState.communityCards.flop(), turn, null);
        return new GameState(gameState.deck, gameState.players, gameState.holeCards, communityCards, gameState.actionHistory);
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
        final List<Position> playersStillInHand = GameState.getPlayersStillInHand(gameState.actionHistory());

        if (gameState.communityCards == null) {
            final List<Position> playersStillInHandOrdered = Position.preFlopOrder(playersStillInHand);
            final List<Action> preFlopActions = actionHistory.preflopActions() == null ? new ArrayList<>() : actionHistory.preflopActions();
            final List<Action> newPreFlopActions = Stream.concat(preFlopActions.stream(), Stream.of(action)).collect(Collectors.toList());
            return new GameState(gameState.deck, gameState.players, gameState.holeCards, gameState.communityCards, new ActionHistory(newPreFlopActions, null, null, null));
        }
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
        throw new IllegalStateException("Did not expect to reach this point. Should be unreachable");
    }

    private static List<Position> getPlayersStillInHand(ActionHistory actionHistory) {
        return null;
    }

    private static Card getCardAt(List<Card> deck, int index) {
        return deck.get(index);
    }
}
