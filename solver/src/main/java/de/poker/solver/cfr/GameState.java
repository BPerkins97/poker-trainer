package de.poker.solver.cfr;

import de.poker.solver.tree.*;

import java.util.*;

import static de.poker.solver.tree.BettingRound.POST_FLOP;
import static de.poker.solver.tree.BettingRound.RIVER;
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
    private 
    private BettingRound bettingRound;
    private Position currentPlayer;
    private int actionsSinceLastRaise = 0;
    private LinkedList<Action> actions = new LinkedList<>();
    private GameConfiguration configuration = GameConfiguration.defaultConfig();

    public GameState() {}
    public GameState(GameState gameState) {
        this.pot = gameState.pot;
        this.currentPlayer = gameState.currentPlayer;
        this.configuration = gameState.configuration;
        this.bettingRound = gameState.bettingRound;
        gameState.players.forEach((key, value) -> this.players.put(key, value));
        this.actionsSinceLastRaise = gameState.actionsSinceLastRaise;
        this.actions = (LinkedList<Action>) gameState.actions.clone();
    }

    public String toInfoSetFor(Position position) {
        // TODO
        return "";
    }

    public Position currentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return false; // TODO
    }

    public Map<Position, Double> handleGameOver() {
        // TODO
        return new HashMap<>();
    }

    // Annahme: Diese Funkion wird nur gecalled, wenn eine Action auch legal wäre
    public List<Action> nextActions() {
        List<Action> result = new ArrayList<>();

        Player player = players.get(currentPlayer);
        double maxInvestment = players.values().stream()
                .map(p -> p.investment)
                .max(Double::compare)
                .orElseThrow();
        double callAmount = maxInvestment - player.investment;
        if (callAmount > 0) {
            result.add(new Action(Action.Type.FOLD, 0));
        }
        assert callAmount >= 0 : "This can't be the player with the biggest investment is at turn";
        result.add(new Action(Action.Type.CALL, callAmount));

        List<Double> raiseAmounts = configuration.raiseAmountsPerRound.get(bettingRound);
        for (double raiseAmount : raiseAmounts) {
            double raiseAmountAbsolute = raiseAmount * pot;
            if (player.stack > raiseAmountAbsolute) {
                result.add(new Action(Action.Type.RAISE, raiseAmountAbsolute));
            }
        }

        double allInAmount = player.stack;
        if (allInAmount > callAmount) {
            result.add(new Action(Action.Type.RAISE, allInAmount));
        }
        return result;
    }

    public GameState takeAction(Action action) {
        return switch (action.type()) {
            case FOLD -> fold();
            case CALL -> call();
            case RAISE -> raise(action.amount());
        };
    }

    private GameState raise(double amount) {
        // TODO
        return new GameState();
    }

    private GameState call() {
        // TODO
        return new GameState();
    }

    private GameState fold() {
        GameState next = new GameState(this);
        infoSet += "f";
        // TODO
        return new GameState();
    }
}
