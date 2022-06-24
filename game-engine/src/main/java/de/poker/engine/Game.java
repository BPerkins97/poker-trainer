package de.poker.engine;

import de.poker.engine.utility.ComparisonConstants;
import de.poker.engine.utility.KeyValue;

import java.util.*;

import static de.poker.engine.Player.Position.*;

public class Game {
    private static final Player.Position[][] BETTING_ORDER_PER_ROUND;

    static {
        BETTING_ORDER_PER_ROUND = new Player.Position[4][];
        BETTING_ORDER_PER_ROUND[0] = new Player.Position[]{LO_JACK, HI_JACK, CUT_OFF, BUTTON, SMALL_BLIND, BIG_BLIND};
        BETTING_ORDER_PER_ROUND[1] = new Player.Position[]{SMALL_BLIND, BIG_BLIND, LO_JACK, HI_JACK, CUT_OFF, BUTTON};
        BETTING_ORDER_PER_ROUND[2] = new Player.Position[]{SMALL_BLIND, BIG_BLIND, LO_JACK, HI_JACK, CUT_OFF, BUTTON};
        BETTING_ORDER_PER_ROUND[3] = new Player.Position[]{SMALL_BLIND, BIG_BLIND, LO_JACK, HI_JACK, CUT_OFF, BUTTON};
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
    private double pot = 0;

    private List<Action> actionHistory = new LinkedList<>(); // TODO Decide later if this linked list makes sense here or if ArrayList is better

    private Game() {}

    private void payBlinds() {
        smallBlind.pay(1);
        bigBlind.pay(2);
        pot = 3;
    }

    public void check(Player.Position position) {
        action(Action.check(position));
    }

    private void action(Action action) {
        if (actionHistory.isEmpty()) {
            payBlinds();
        }
        actionHistory.add(action);
        testAndHandleGameOver();
    }

    private Player getPlayer(Player.Position position) {
        return switch (position) {
            case LO_JACK -> loJack;
            case CUT_OFF -> cutOff;
            case HI_JACK -> hiJack;
            case BUTTON -> button;
            case BIG_BLIND -> bigBlind;
            case SMALL_BLIND -> smallBlind;
        };
    }

    private void handleGameOver(List<Player.Position> playersStillInGame) {
        if (playersStillInGame.size() == 1) {
            Player winner = getPlayer(playersStillInGame.get(0));
            winner.win(pot);
            return;
        }

        List<Player> players = playersStillInGame.stream()
                .map(this::getPlayer)
                .toList();


        // Winner is at the bottom of the list
        List<KeyValue<Player.Position, Hand>> positionWinners = players.stream()
                .map(player -> {
                    Hand hand = Hand.of(player.holeCards(), flop, turn, river);
                    return new KeyValue<>(player.position(), hand);
                })
                .sorted(Comparator.comparing(KeyValue::value))
                .toList();
        List<Player.Position> winners = new ArrayList<>();
        winners.add(positionWinners.get(5).key());
        int index = 4;
        while (positionWinners.get(index).value().compareTo(positionWinners.get(5).value()) == ComparisonConstants.X_EQUAL_TO_Y) {
            winners.add(positionWinners.get(index).key());
        }
        double sharedPot = pot / winners.size();
        winners.stream()
                .map(this::getPlayer)
                .forEach(player -> player.win(sharedPot));
    }

    // TODO instead of calculating this each time someone takes an action only calculate the effect of the action he took
    private void testAndHandleGameOver() {
        int bettingRound = 0;
        int playerIndex = 0;
        List<Player.Position> positionsStillInGame = new LinkedList<>(List.of(values()));

        for (Action action : actionHistory) {
            if (BETTING_ORDER_PER_ROUND[bettingRound][playerIndex].equals(action.position())) {
                if (action.type().equals(Action.Type.CHECK)) {
                    do {
                        if (playerIndex >= 5) {
                            if (bettingRound < 3) {
                                bettingRound++;
                                playerIndex = 0;
                            } else {
                                handleGameOver(positionsStillInGame);
                                return;
                            }
                        } else {
                            playerIndex++;
                        }
                    } while (!positionsStillInGame.contains(BETTING_ORDER_PER_ROUND[bettingRound][playerIndex]));
                }
                if (action.type().equals(Action.Type.FOLD)) {
                    positionsStillInGame.remove(action.position());
                    if (positionsStillInGame.size() == 1) {
                        handleGameOver(positionsStillInGame);
                    }
                    do {
                        if (playerIndex >= 5) {
                            if (bettingRound < 3) {
                                bettingRound++;
                                playerIndex = 0;
                            } else {
                                handleGameOver(positionsStillInGame);
                                return;
                            }
                        } else {
                            playerIndex++;
                        }
                    } while (!positionsStillInGame.contains(BETTING_ORDER_PER_ROUND[bettingRound][playerIndex]));
                }
            } else {
                assert false : "We took a wrong action at some point";
            }
        }
        return;
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
}
