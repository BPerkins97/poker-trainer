package de.poker.engine;

import de.poker.engine.utility.ComparisonConstants;
import de.poker.engine.utility.KeyValue;

import java.util.*;

import static de.poker.engine.Player.Position.*;

public class Game {
    private static final Map<BettingRound, List<Player.Position>> BETTING_ORDER_PER_ROUND;

    static {
        BETTING_ORDER_PER_ROUND = new HashMap<>();
        BETTING_ORDER_PER_ROUND.put(BettingRound.PRE_FLOP, Arrays.asList(LO_JACK, HI_JACK, CUT_OFF, BUTTON, SMALL_BLIND, BIG_BLIND));
        List<Player.Position> normalOrder = Arrays.asList(SMALL_BLIND, BIG_BLIND, LO_JACK, HI_JACK, CUT_OFF, BUTTON);
        BETTING_ORDER_PER_ROUND.put(BettingRound.POST_FLOP, normalOrder);
        BETTING_ORDER_PER_ROUND.put(BettingRound.TURN, normalOrder);
        BETTING_ORDER_PER_ROUND.put(BettingRound.RIVER, normalOrder);
    }

    private Player smallBlind;
    private Player bigBlind;
    private Player loJack;
    private Player hiJack;
    private Player cutOff;
    private Player button;
    private Flop flop;
    private Card turn;
    private Card river;

    private BettingRound bettingRound = BettingRound.PRE_FLOP;
    private Player nextPlayer;
    private int actionsSinceLastRaise = 0;

    private Game() {
    }

    private void payBlinds() {
        smallBlind().pay(1);
        bigBlind().pay(2);
    }

    public void check(Player.Position position) {
        action(Action.check(position));
    }

    private void action(Action action) {
        if (nextPlayer == null) {
            payBlinds();
            nextPlayer = loJack();
        }
        assert action.position() == nextPlayer.position();

        switch (action.type()) {
            case FOLD -> handleFold();
            case CALL -> handleCall();
            case RAISE -> handleRaise(action.amount());
        }

        actionsSinceLastRaise++;

        List<Player> playersStillInGame = players().stream()
                .filter(player -> !player.hasFolded())
                .toList();

        List<Player> playersNotAllIn = players().stream()
                .filter(player -> !player.isAllIn())
                .toList();

        boolean isEndOfBettingRound = actionsSinceLastRaise >= playersStillInGame.size();

        if (playersNotAllIn.size() <= 1 && isEndOfBettingRound) {
            handleShowdown(playersStillInGame);
        }

        if (playersStillInGame.size() == 1) {
            handleEveryoneFolded(playersStillInGame.get(0));
        }

        boolean investementsDiffer = false;
        int lastPlayerToActIndex = BETTING_ORDER_PER_ROUND.get(bettingRound).indexOf(playersStillInGame.get(0).position());
        double investment = playersStillInGame.get(0).investment();
        for (int i = 1; i < playersStillInGame.size(); i++) {
            if (investment != playersStillInGame.get(i).investment()) {
                investementsDiffer = true;
            }
            int actIndex = BETTING_ORDER_PER_ROUND.get(bettingRound).indexOf(playersStillInGame.get(i).position());
            if (actIndex > lastPlayerToActIndex) {
                lastPlayerToActIndex = actIndex;
            }
        }
        int currentPlayerIndex = BETTING_ORDER_PER_ROUND.get(bettingRound).indexOf(nextPlayer.position());

        boolean determineNextPlayer = false;

        if (!investementsDiffer && isEndOfBettingRound) {
            bettingRound = switch (bettingRound) {
                case PRE_FLOP -> BettingRound.POST_FLOP;
                case POST_FLOP -> BettingRound.TURN;
                case TURN -> BettingRound.RIVER;
                case RIVER -> null;
            };
            actionsSinceLastRaise = 0;
            if (bettingRound == null) {
                handleShowdown(playersStillInGame);
            } else {
                determineNextPlayer = true;
                currentPlayerIndex = 5;
            }
        } else {
            determineNextPlayer = true;
        }

        if (determineNextPlayer) {
            int nextPlayerIndex = currentPlayerIndex;
            do {
                nextPlayerIndex = nextPlayerIndex >= 5 ? 0 : nextPlayerIndex + 1;

                nextPlayer = getPlayer(BETTING_ORDER_PER_ROUND.get(bettingRound).get(nextPlayerIndex));
            } while (nextPlayer.hasFolded());
        }

        // TODO add action to history
    }

    private void handleEveryoneFolded(Player winner) {
        winner.win(calculatePot());
    }

    private double calculatePot() {
        double pot = players().stream()
                .map(Player::investment)
                .reduce(Double::sum)
                .orElse(0.0);
        return pot;
    }

    private void handleShowdown(List<Player> players) {
        // Winner is at the bottom of the list
        List<KeyValue<Player.Position, Hand>> positionWinners = players.stream()
                .map(player -> {
                    Hand hand = Hand.of(player.holeCards(), flop, turn, river);
                    return new KeyValue<>(player.position(), hand);
                })
                .sorted(Comparator.comparing(KeyValue::value))
                .toList();
        List<Player.Position> winners = new ArrayList<>();
        int winnerIndex = positionWinners.size() - 1;
        winners.add(positionWinners.get(winnerIndex).key());
        int index = winnerIndex-1;
        while (index >= 0 && positionWinners.get(index).value().compareTo(positionWinners.get(winnerIndex).value()) == ComparisonConstants.X_EQUAL_TO_Y) {
            winners.add(positionWinners.get(index).key());
            index--;
        }
        double sharedPot = calculatePot() / winners.size();
        winners.stream()
                .map(this::getPlayer)
                .forEach(player -> player.win(sharedPot));
    }

    private Player getPlayer(Player.Position position) {
        return switch (position) {
            case SMALL_BLIND -> smallBlind();
            case BIG_BLIND -> bigBlind();
            case BUTTON -> button();
            case HI_JACK -> hiJack();
            case CUT_OFF -> cutOff();
            case LO_JACK -> loJack();
        };
    }

    private void handleRaise(double amount) {
        nextPlayer.pay(amount);
        actionsSinceLastRaise = 0;
        // TODO If he reraises someone he has to raise at least the double amount -> research this rule, not sure about this
        // TODO check if player is all in
        // TODO Player has to raise at least the big blind
    }

    private void handleCall() {
        // find biggest investment of all players
        // pay the difference between biggest investment and your investment
        // TODO check if player is all in
        double max = players().stream()
                .map(Player::investment)
                .max(Double::compareTo)
                .orElse(0.0);
        double hasToPay = max - nextPlayer.investment();
        nextPlayer.pay(hasToPay);
    }

    private void handleFold() {
        nextPlayer.fold();
    }

    private List<Player> players() {
        return Arrays.asList(
                smallBlind(),
                bigBlind(),
                loJack(),
                hiJack(),
                cutOff(),
                button()
        );
    }

    public Player smallBlind() {
        return smallBlind;
    }

    public Player bigBlind() {
        return bigBlind;
    }

    public Player loJack() {
        return loJack;
    }

    public Player hiJack() {
        return hiJack;
    }

    public Player cutOff() {
        return cutOff;
    }

    public Player button() {
        return button;
    }

    public void fold(Player.Position position) {
        action(Action.fold(position));
    }

    public void raise(Player.Position position, double amount) {
        action(Action.raise(position, amount));
    }

    public void call(Player.Position position) {
        action(Action.call(position));
    }

    public static class Factory {
        private double startingStack;
        private Game game = new Game();

        public static Factory newGame() {
            return new Factory();
        }

        public Factory smallBlind(String card1, String card2) {
            game.smallBlind = Player.smallBlind(startingStack, card1, card2);
            return this;
        }

        public Factory bigBlind(String card1, String card2) {
            game.bigBlind = Player.bigBlind(startingStack, card1, card2);
            return this;
        }

        public Factory loJack(String card1, String card2) {
            game.loJack = Player.loJack(startingStack, card1, card2);
            return this;
        }

        public Factory hiJack(String card1, String card2) {
            game.hiJack = Player.hiJack(startingStack, card1, card2);
            return this;
        }

        public Factory cutOff(String card1, String card2) {
            game.cutOff = Player.cutOff(startingStack, card1, card2);
            return this;
        }

        public Factory button(String card1, String card2) {
            game.button = Player.button(startingStack, card1, card2);
            return this;
        }

        public Factory startingStacks(int startingStack) {
            this.startingStack = startingStack;
            return this;
        }

        public Factory flop(String card1, String card2, String card3) {
            game.flop = Flop.of(card1, card2, card3);
            return this;
        }

        public Factory turn(String card) {
            game.turn = Card.of(card);
            return this;
        }

        public Factory river(String card) {
            game.river = Card.of(card);
            return this;
        }

        public Game build() {
            return game;
        }
    }

    private enum BettingRound {
        PRE_FLOP,
        POST_FLOP,
        TURN,
        RIVER
    }
}
