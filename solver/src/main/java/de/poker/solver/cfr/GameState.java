package de.poker.solver.cfr;

import de.poker.solver.tree.BettingRound;
import de.poker.solver.tree.GameConfiguration;
import de.poker.solver.tree.KeyValue;
import de.poker.solver.tree.Position;
import de.poker.solver.utility.ComparisonConstants;

import java.util.*;

import static de.poker.solver.tree.BettingRound.*;
import static de.poker.solver.tree.Position.*;

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
    private LinkedList<Action> actions = new LinkedList<>();
    private GameConfiguration configuration = GameConfiguration.defaultConfig();
    private boolean isGameOverByAllFolded = false;
    private boolean isGameOverByShowdown = false;

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
        flop = new Flop(configuration.cards.get(cardCounter), configuration.cards.get(cardCounter+1), configuration.cards.get(cardCounter+2));
        turn = configuration.cards.get(cardCounter+3);
        river = configuration.cards.get(cardCounter+4);
        determineNextPlayer();
    }
    public GameState(GameState gameState) {
        this.pot = gameState.pot;
        this.currentPlayer = gameState.currentPlayer;
        this.configuration = gameState.configuration;
        this.bettingRound = gameState.bettingRound;
        gameState.players.forEach((key, value) -> this.players.put(key, new Player(value)));
        this.actionsSinceLastRaise = gameState.actionsSinceLastRaise;
        this.actions = (LinkedList<Action>) gameState.actions.clone();
        this.flop = gameState.flop;
        this.turn = gameState.turn;
        this.river = gameState.river;

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
        Player player = players.get(position);
        infoSetBuilder.append(player.holeCards().asInfoSet());
        if (bettingRound.equals(RIVER)) {
            infoSetBuilder
                    .append(flop.card1().forInfoSet())
                    .append(flop.card2().forInfoSet())
                    .append(flop.card3().forInfoSet())
                    .append(turn.forInfoSet())
                    .append(river.forInfoSet());
        } else if (bettingRound.equals(TURN)) {
            infoSetBuilder
                    .append(flop.card1().forInfoSet())
                    .append(flop.card2().forInfoSet())
                    .append(flop.card3().forInfoSet())
                    .append(turn.forInfoSet());
        } else if(bettingRound.equals(POST_FLOP)) {
            infoSetBuilder
                    .append(flop.card1().forInfoSet())
                    .append(flop.card2().forInfoSet())
                    .append(flop.card3().forInfoSet());
        }
        Iterator<Action> iterator = actions.iterator();
        while (iterator.hasNext()) {
            infoSetBuilder.append(iterator.next().toString());
        }
        return infoSetBuilder.toString();
    }

    public Position currentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return isGameOverByAllFolded || isGameOverByShowdown;
    }

    public Map<Position, Double> handleGameOver() {
        List<Position> playersStillInGame = getPlayersStillInGame();
        Map<Position, Double> winnings = new HashMap<>();
        players.forEach((key, value) -> winnings.put(key, -value.investment()));

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
                    return new KeyValue<>(entry.getKey(), Hand.of(cards));
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
            result.add(new Action(Action.Type.FOLD, 0));
        }
        assert callAmount >= 0 : "This can't be the player with the biggest investment is at turn";
        result.add(new Action(Action.Type.CALL, callAmount));

        List<Double> raiseAmounts = configuration.raiseAmountsPerRound.get(bettingRound);
        for (double raiseAmount : raiseAmounts) {
            double raiseAmountAbsolute = raiseAmount * pot;
            if (player.stack() > raiseAmountAbsolute) {
                result.add(new Action(Action.Type.RAISE, raiseAmountAbsolute));
            }
        }

        double allInAmount = player.stack();
        if (allInAmount > callAmount) {
            result.add(new Action(Action.Type.RAISE, allInAmount));
        }
        return result;
    }

    public GameState takeAction(Action action) {
        GameState next = new GameState(this);
        switch (action.type()) {
            case FOLD: {
                next.fold();
                break;
            }
            case CALL: {
                next.call();
                break;
            }
            case RAISE: {
                next.raise(action.amount());
                break;
            }
        }
        next.actions.add(action);
        next.determineNextPlayer();
        return next;
    }

    private void determineNextPlayer() {
        actionsSinceLastRaise++;

        // TODO we could cache this value in order to improve performance
        List<Position> playersStillInGame = getPlayersStillInGame();

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
                case PRE_FLOP: nextBettingRound(POST_FLOP);
                case POST_FLOP: nextBettingRound(TURN);
                case TURN: nextBettingRound(RIVER);
                case RIVER: isGameOverByShowdown = true;
            }
        } else {
            int nextPlayerIndex = currentPlayerIndex;
            do {
                nextPlayerIndex = nextPlayerIndex >= 5 ? 0 : nextPlayerIndex + 1;

                currentPlayer = BETTING_ORDER_PER_ROUND.get(bettingRound).get(nextPlayerIndex);
            } while (players.get(currentPlayer).hasFolded());
        }
    }

    private List<Position> getPlayersStillInGame() {
        List<Position> playersStillInGame = players.entrySet().stream()
                .filter(player -> !player.getValue().hasFolded())
                .map(Map.Entry::getKey)
                .toList();
        return playersStillInGame;
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
        players.get(currentPlayer).fold();
    }
}
