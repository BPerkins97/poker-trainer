package de.poker.solver.cfr;

import de.poker.solver.utility.ComparisonConstants;
import de.poker.solver.utility.KeyValue;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.poker.solver.cfr.BettingRound.*;
import static de.poker.solver.cfr.Position.*;

public class GameState {
    private static final Map<BettingRound, List<Position>> BETTING_ORDER_PER_ROUND;

    static {
        BETTING_ORDER_PER_ROUND = new HashMap<>();
        BETTING_ORDER_PER_ROUND.put(BettingRound.PRE_FLOP, Arrays.asList(LO_JACK, HI_JACK, CUT_OFF, BUTTON, SMALL_BLIND, BIG_BLIND));
        List<Position> normalOrder = Arrays.asList(SMALL_BLIND, BIG_BLIND, LO_JACK, HI_JACK, CUT_OFF, BUTTON);
        BETTING_ORDER_PER_ROUND.put(POST_FLOP, normalOrder);
        BETTING_ORDER_PER_ROUND.put(BettingRound.TURN, normalOrder);
        BETTING_ORDER_PER_ROUND.put(RIVER, normalOrder);
    }

    private double pot;
    private Map<Position, Player> players = new HashMap<>();
    private Flop flop;
    private Card turn;
    private Card river;
    private BettingRound bettingRound;
    private Position currentPlayer;
    private int actionsSinceLastRaise = 0;
    private StringBuilder actionInfoSetBuilder = new StringBuilder();
    private GameConfiguration configuration = GameConfiguration.defaultConfig();
    private boolean isGameOverByAllFolded = false;
    private boolean isGameOverByShowdown = false;
    private List<Position> playersStillInGame;

    public GameState(GameConfiguration configuration) {
        this.configuration = configuration;
        pot = configuration.smallBlind + configuration.bigBlind;
        bettingRound = PRE_FLOP;
        int cardCounter = 0;
        for (Map.Entry<Position, Double> stack : configuration.stacks.entrySet()) {
            HoleCards holeCards = new HoleCards(configuration.cards.get(cardCounter), configuration.cards.get(cardCounter+1));
            Player player = new Player(stack.getValue(), holeCards);
            if (stack.getKey().equals(SMALL_BLIND)) {
                player.pay(configuration.smallBlind);
            } else if (stack.getKey().equals(BIG_BLIND)) {
                player.pay(configuration.bigBlind);
            }
            players.put(stack.getKey(), player);
            cardCounter += 2;
        }
        flop = Flop.of(configuration.cards.get(cardCounter), configuration.cards.get(cardCounter+1), configuration.cards.get(cardCounter+2));
        turn = configuration.cards.get(cardCounter+3);
        river = configuration.cards.get(cardCounter+4);
        playersStillInGame = new ArrayList<>(players.keySet());
        determineNextPlayer();
    }
    public GameState(GameState gameState) {
        this.pot = gameState.pot;
        this.currentPlayer = gameState.currentPlayer;
        this.configuration = gameState.configuration;
        this.bettingRound = gameState.bettingRound;
        gameState.players.forEach((key, value) -> this.players.put(key, new Player(value)));
        this.actionsSinceLastRaise = gameState.actionsSinceLastRaise;
        this.actionInfoSetBuilder = new StringBuilder(gameState.actionInfoSetBuilder);
        this.flop = gameState.flop;
        this.turn = gameState.turn;
        this.river = gameState.river;
        this.playersStillInGame = gameState.playersStillInGame;
    }

    public String toInfoSetFor(Position position) {
        // TODO when we are in pre flop phase, then only print cards out like 98o, 65s, etc.
        StringBuilder infoSetBuilder = new StringBuilder();
        infoSetBuilder.append(
                switch (position) {
                    case SMALL_BLIND -> "1";
                    case BIG_BLIND -> "2";
                    case LO_JACK -> "3";
                    case HI_JACK -> "4";
                    case CUT_OFF -> "5";
                    case BUTTON -> "6";
                }
        );

        // TODO order by round. Because river is the last round it will be always called least often. Therefore we can increase performance by calling postflop first
        // TODO Refactor these asInfoSet Methods to accept a StringBuilder and use that one to reduce String creation overhead
        Player player = players.get(position);
        if (bettingRound.equals(PRE_FLOP)) {
            player.holeCards().appendReducedInfoSet(infoSetBuilder);
        } else if (bettingRound.equals(RIVER)) {
            infoSetBuilder
                    .append(player.holeCards().asInfoSet())
                    .append(flop.card1().forInfoSet())
                    .append(flop.card2().forInfoSet())
                    .append(flop.card3().forInfoSet())
                    .append(turn.forInfoSet())
                    .append(river.forInfoSet());
        } else if (bettingRound.equals(TURN)) {
            infoSetBuilder
                    .append(player.holeCards().asInfoSet())
                    .append(flop.card1().forInfoSet())
                    .append(flop.card2().forInfoSet())
                    .append(flop.card3().forInfoSet())
                    .append(turn.forInfoSet());
        } else if(bettingRound.equals(POST_FLOP)) {
            infoSetBuilder
                    .append(player.holeCards().asInfoSet())
                    .append(flop.card1().forInfoSet())
                    .append(flop.card2().forInfoSet())
                    .append(flop.card3().forInfoSet());
        }
        // TODO action to string
        infoSetBuilder.append(actionInfoSetBuilder.toString());
        return infoSetBuilder.toString();
    }

    public Position currentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return isGameOverByAllFolded || isGameOverByShowdown;
    }

    public Map<Position, Double> handleGameOver() {
        Map<Position, Double> winnings = new HashMap<>();
        players.forEach((key, value) -> winnings.put(key, -value.investment()));

        // TODO Potentially cache this on object
        double pot = players.values().stream()
                .map(Player::investment)
                .reduce(Double::sum)
                .orElse(0.0);

        if (isGameOverByAllFolded) {
            Position winner = playersStillInGame.get(0);
            double investment = players.get(winner).investment();
            winnings.put(winner, pot - investment);
            return winnings;
        }

        AtomicInteger maxValue = new AtomicInteger(0);
        List<KeyValue<Position, Hand>> hands = players.entrySet()
                .stream()
                .filter(entry -> !entry.getValue().hasFolded())
                .map(entry -> {
                    List<Card> cards = new ArrayList<>(7);
                    cards.add(flop.card1());
                    cards.add(flop.card2());
                    cards.add(flop.card3());
                    cards.add(turn);
                    cards.add(river);
                    cards.add(entry.getValue().holeCards().card1());
                    cards.add(entry.getValue().holeCards().card2());
                    Hand of = Hand.of(cards);
                    if (of.value > maxValue.get()) {
                        //maxValue.set(of.value);
                    }
                    return new KeyValue<>(entry.getKey(), of);
                })
                .sorted(Comparator.comparing(KeyValue::value))
                .toList();

        List<Position> winners = new ArrayList<>();
        int winnerIndex = hands.size() - 1;
        winners.add(hands.get(winnerIndex).key());
        int index = winnerIndex-1;
        while (index >= 0 && hands.get(index).value().compareTo(hands.get(winnerIndex).value()) == ComparisonConstants.X_EQUAL_TO_Y) {
            winners.add(hands.get(index).key());
            index--;
        }
        double sharedPot = pot / winners.size();
        winners.stream()
                .forEach(player -> winnings.put(player, sharedPot - players.get(player).investment()));
        return winnings;
    }

    // Annahme: Diese Funkion wird nur gecalled, wenn eine Action auch legal wäre
    public List<Action> nextActions() {
        List<Action> result = new ArrayList<>();

        Player player = players.get(currentPlayer);
        double maxInvestment = players.values().stream()
                .map(Player::investment)
                .max(Double::compare)
                .orElseThrow();
        double callAmount = maxInvestment - player.investment();
        if (callAmount > 0) {
            result.add(Action.FOLD);
        }
        assert callAmount >= 0 : "This can't be the player with the biggest investment is at turn";
        result.add(Action.CALL);

        List<Double> raiseAmounts = configuration.raiseAmountsPerRound.get(bettingRound);
        for (double raiseAmount : raiseAmounts) {
            double raiseAmountAbsolute = raiseAmount * pot;
            if (player.stack() > raiseAmountAbsolute) {
                result.add(Action.raise(raiseAmountAbsolute));
            }
        }

        double allInAmount = player.stack();
        if (allInAmount > callAmount) {
            result.add(Action.raise(allInAmount));
        }
        return result;
    }

    public GameState takeAction(Action action) {
        GameState next = new GameState(this);
        if (action.isFold()) {
            next.fold();
        } else if (action.isCall()) {
            next.call();
        } else {
            next.raise(action.amount);
        }
        actionInfoSetBuilder.append(action);
        next.determineNextPlayer();
        return next;
    }

    private void determineNextPlayer() {
        actionsSinceLastRaise++;

        boolean isEndOfBettingRound = actionsSinceLastRaise >= playersStillInGame.size();

        if (playersStillInGame.size() == 1) {
            this.isGameOverByAllFolded = true;
            return;
        }

        boolean investementsDiffer = false;
        int lastPlayerToActIndex = BETTING_ORDER_PER_ROUND.get(bettingRound).indexOf(playersStillInGame.get(0));
        double investment = players.get(playersStillInGame.get(0)).investment();
        for (int i = 1; i < playersStillInGame.size(); i++) {
            if (investment != players.get(playersStillInGame.get(i)).investment()) {
                investementsDiffer = true;
            }
            int actIndex = BETTING_ORDER_PER_ROUND.get(bettingRound).indexOf(playersStillInGame.get(i));
            if (actIndex > lastPlayerToActIndex) {
                lastPlayerToActIndex = actIndex;
            }
        }
        int currentPlayerIndex = BETTING_ORDER_PER_ROUND.get(bettingRound).indexOf(currentPlayer);

        if (!investementsDiffer && isEndOfBettingRound) {
            switch (bettingRound) {
                case PRE_FLOP: {
                    nextBettingRound(POST_FLOP);
                    break;
                }
                case POST_FLOP: {
                    nextBettingRound(TURN);
                    break;
                }
                case TURN: {
                    nextBettingRound(RIVER);
                    break;
                }
                case RIVER: {
                    isGameOverByShowdown = true;
                    break;
                }
            }
        } else {
            int nextPlayerIndex = currentPlayerIndex;
            do {
                nextPlayerIndex = nextPlayerIndex >= 5 ? 0 : nextPlayerIndex + 1;

                currentPlayer = BETTING_ORDER_PER_ROUND.get(bettingRound).get(nextPlayerIndex);
            } while (players.get(currentPlayer).hasFolded());
        }
    }

    private void nextBettingRound(BettingRound nextBettingRound) {
        bettingRound = nextBettingRound;
        actionsSinceLastRaise = 0;

        int nextPlayerIndex = 0;
        while (players.get(BETTING_ORDER_PER_ROUND.get(bettingRound).get(nextPlayerIndex)).hasFolded()) {
            assert nextPlayerIndex < 5 : "Oops, something went wrong when determining the next player";
            nextPlayerIndex++;
        }
        currentPlayer = BETTING_ORDER_PER_ROUND.get(bettingRound).get(nextPlayerIndex);
    }

    private void raise(double amount) {
        players.get(currentPlayer).pay(amount);
        actionsSinceLastRaise = 0;
        pot += amount;
    }

    private void call() {
        Double highestInvestment = players.values()
                .stream()
                .map(Player::investment)
                .max(Double::compareTo)
                .orElse(0.0);
        Player nextPlayer = players.get(currentPlayer);
        double payAmount = highestInvestment - nextPlayer.investment();
        nextPlayer.pay(payAmount);
        pot += payAmount;
    }

    private void fold() {
        playersStillInGame = new ArrayList<>(playersStillInGame);
        playersStillInGame.remove(currentPlayer);
        players.get(currentPlayer).fold();
    }
}
