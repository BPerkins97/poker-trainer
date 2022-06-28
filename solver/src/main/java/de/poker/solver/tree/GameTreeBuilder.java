package de.poker.solver.tree;

import de.poker.solver.cfr.GameTreeNode;

import java.util.*;

import static de.poker.solver.tree.BettingRound.*;
import static de.poker.solver.cfr.GameTreeNode.Type.*;
import static de.poker.solver.tree.Position.*;

public class GameTreeBuilder {
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
    private BettingRound bettingRound;
    private Position nextPlayer;
    private int actionsSinceLastRaise = 0;
    private List<GameTreeBuilder> children = new LinkedList<>();

    private GameTreeNode.Type type;
    private double raiseAmount = 0;
    private GameConfiguration configuration = GameConfiguration.defaultConfig();

    public static GameTreeBuilder build(GameConfiguration configuration) {
        // TODO maybe copy configuration for safety
        GameTreeBuilder gameState = new GameTreeBuilder();
        gameState.configuration = configuration;
        gameState.pot = configuration.smallBlind + configuration.bigBlind;
        gameState.type = DEAL_HOLE_CARDS;
        gameState.bettingRound = PRE_FLOP;
        configuration.stacks
                .entrySet().stream()
                .map(entry -> {
                    Player player = new Player();
                    player.stack = entry.getValue();
                    if (entry.getKey() == SMALL_BLIND) {
                        player.pay(configuration.smallBlind);
                    } else if (entry.getKey() == BIG_BLIND) {
                        player.pay(configuration.bigBlind);
                    }
                    return new KeyValue<>(entry.getKey(), player);
                })
                .forEach(entry -> gameState.players.put(entry.key(), entry.value()));
        gameState.determineNextPlayer().nextAction();
        return gameState;
    }

    public GameTreeNode toNode() {
        List<GameTreeNode> childGameTreeNodes = children.stream()
                .map(GameTreeBuilder::toNode)
                .toList();
        GameTreeNode gameTreeNode = new GameTreeNode(type, childGameTreeNodes, raiseAmount);
        return gameTreeNode;
    }

    private GameTreeBuilder copy() {
        GameTreeBuilder gameState = new GameTreeBuilder();
        gameState.bettingRound = bettingRound;
        gameState.configuration = configuration;
        gameState.pot = pot;
        gameState.nextPlayer = nextPlayer;
        gameState.actionsSinceLastRaise = actionsSinceLastRaise;
        for (Position position : Position.values()) {
            if (players.containsKey(position)) {
                gameState.players.put(position, players.get(position).copy());
            }
        }
        return gameState;
    }

    private void nextAction() {
        if (type == SHOWDOWN || type == EVERYONE_FOLDED) {
            return;
        }
        Player player = players.get(nextPlayer);
        if (player.isAllIn()) {
            determineNextPlayer().nextAction();
            return;
        }
        double maxInvestment = players.values().stream()
                .map(p -> p.investment)
                .max(Double::compare)
                .orElseThrow();
        double callAmount = maxInvestment - players.get(nextPlayer).investment;
        if (callAmount > 0) {
            fold();
        }
        assert callAmount >= 0 : "This can't be the player with the biggest investment is at turn";
        call(callAmount);

        List<Double> raiseAmounts = configuration.raiseAmountsPerRound.get(bettingRound);
        for (double raiseAmount : raiseAmounts) {
            double raiseAmountAbsolute = raiseAmount * pot;
            if (player.stack > raiseAmountAbsolute) {
                raise(raiseAmountAbsolute);
            }
        }

        double allInAmount = player.stack;
        if (allInAmount > callAmount) {
            raise(allInAmount);
        }
    }

    public void fold() {
        GameTreeBuilder gameState = this.copy();
        gameState.players.get(nextPlayer).fold();
        gameState.type = FOLD;
        children.add(gameState);
        gameState.determineNextPlayer().nextAction();
    }

    private GameTreeBuilder determineNextPlayer() {
        actionsSinceLastRaise++;

        List<Position> playersStillInGame = players.entrySet().stream()
                .filter(player -> !player.getValue().hasFolded())
                .map(Map.Entry::getKey)
                .toList();

        boolean isEndOfBettingRound = actionsSinceLastRaise >= playersStillInGame.size();

        if (playersStillInGame.size() == 1) {
            return everyoneFolded();
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
        int currentPlayerIndex = BETTING_ORDER_PER_ROUND.get(bettingRound).indexOf(nextPlayer);

        if (!investementsDiffer && isEndOfBettingRound) {
            return switch (bettingRound) {
                case PRE_FLOP -> nextBettingRound(POST_FLOP);
                case POST_FLOP -> nextBettingRound(TURN);
                case TURN -> nextBettingRound(RIVER);
                case RIVER -> showdown();
            };
        } else {
            int nextPlayerIndex = currentPlayerIndex;
            do {
                nextPlayerIndex = nextPlayerIndex >= 5 ? 0 : nextPlayerIndex + 1;

                nextPlayer = BETTING_ORDER_PER_ROUND.get(bettingRound).get(nextPlayerIndex);
            } while (players.get(nextPlayer).hasFolded());
        }
        return this;
    }

    private GameTreeBuilder nextBettingRound(BettingRound nextBettingRound) {
        GameTreeBuilder gameState = this.copy();
        gameState.bettingRound = nextBettingRound;
        gameState.actionsSinceLastRaise = 0;
        gameState.type = switch (nextBettingRound) {
            case TURN -> DEAL_TURN;
            case PRE_FLOP -> throw new IllegalArgumentException();
            case RIVER -> DEAL_RIVER;
            case POST_FLOP -> DEAL_FLOP;
        };

        int nextPlayerIndex = 0;
        while (players.get(BETTING_ORDER_PER_ROUND.get(bettingRound).get(nextPlayerIndex)).hasFolded()) {
            assert nextPlayerIndex < 5 : "Oops, something went wrong when determining the next player";
            nextPlayerIndex++;
        }
        gameState.nextPlayer = BETTING_ORDER_PER_ROUND.get(bettingRound).get(nextPlayerIndex);
        children.add(gameState);
        return gameState;
    }

    private GameTreeBuilder everyoneFolded() {
        GameTreeBuilder copy = this.copy();
        copy.type = EVERYONE_FOLDED;
        children.add(copy);
        return copy;
    }

    private GameTreeBuilder showdown() {
        GameTreeBuilder copy = this.copy();
        copy.type = SHOWDOWN;
        children.add(copy);
        return copy;
    }

    public void call(double amount) {
        GameTreeBuilder gameState = this.copy();
        gameState.players.get(nextPlayer).pay(amount);
        gameState.type = CALL;
        gameState.pot += amount;
        children.add(gameState);
        gameState.determineNextPlayer().nextAction();
    }

    public void raise(double amount) {
        GameTreeBuilder gameState = this.copy();
        gameState.players.get(nextPlayer).pay(amount);
        gameState.actionsSinceLastRaise = 0;
        gameState.pot += amount;
        gameState.type = RAISE;
        gameState.raiseAmount = amount;
        children.add(gameState);
        gameState.determineNextPlayer().nextAction();
    }
}
