package de.poker.solver;

import java.util.*;
import java.util.stream.Collectors;

import static de.poker.solver.BettingRound.*;
import static de.poker.solver.Node.Type.*;
import static de.poker.solver.Position.*;

public class Node {
    private Type type;
    private List<Node> children = new ArrayList<>();
    private double amount = 0;

    public int countLeafes() {
        if (type == SHOWDOWN || type == EVERYONE_FOLDED) {
            return 1;
        }
        return children
                .stream()
                .map(Node::countLeafes)
                .reduce(Integer::sum)
                .orElseThrow();
    }

    public String asString(String indentation) {
        String str = indentation + type;
        if (amount > 0) {
            str += " " + amount;
        }
        String childrenStrs = children
                .stream()
                .map(c -> c.asString(indentation + "  "))
                .collect(Collectors.joining());
        return str + "\n" + childrenStrs;
    }
    public enum Type {
        DEAL_HOLE_CARDS,
        FOLD,
        CALL,
        RAISE,
        SHOWDOWN,
        EVERYONE_FOLDED,
        DEAL_FLOP,
        DEAL_TURN,
        DEAL_RIVER

    }
    public static class GameState {
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
        private List<GameState> children = new LinkedList<>();

        private Type type;
        private double raiseAmount = 0;
        private GameConfiguration configuration = GameConfiguration.defaultConfig();

        public static GameState build(GameConfiguration configuration) {
            // TODO maybe copy configuration for safety
            GameState gameState = new GameState();
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

        public Node toNode() {
            Node node = new Node();
            node.type = type;
            node.amount = raiseAmount;
            node.children = children.stream()
                    .map(GameState::toNode)
                    .toList();
            return node;
        }
        private GameState copy() {
            GameState gameState = new GameState();
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
            GameState gameState = this.copy();
            gameState.players.get(nextPlayer).fold();
            gameState.type = FOLD;
            children.add(gameState);
            gameState.determineNextPlayer().nextAction();
        }

        private GameState determineNextPlayer() {
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

        private GameState nextBettingRound(BettingRound nextBettingRound) {
            GameState gameState = this.copy();
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

        private GameState everyoneFolded() {
            GameState copy = this.copy();
            copy.type = EVERYONE_FOLDED;
            children.add(copy);
            return copy;
        }

        private GameState showdown() {
            GameState copy = this.copy();
            copy.type = SHOWDOWN;
            children.add(copy);
            return copy;
        }

        public void call(double amount) {
            GameState gameState = this.copy();
            gameState.players.get(nextPlayer).pay(amount);
            gameState.type = CALL;
            gameState.pot += amount;
            children.add(gameState);
            gameState.determineNextPlayer().nextAction();
        }

        public void raise(double amount) {
            GameState gameState = this.copy();
            gameState.players.get(nextPlayer).pay(amount);
            gameState.actionsSinceLastRaise = 0;
            gameState.pot += amount;
            gameState.type = RAISE;
            gameState.raiseAmount = amount;
            children.add(gameState);
            gameState.determineNextPlayer().nextAction();
        }
    }
}
