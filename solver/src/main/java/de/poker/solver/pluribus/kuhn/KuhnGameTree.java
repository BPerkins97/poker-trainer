package de.poker.solver.pluribus.kuhn;

import de.poker.solver.pluribus.GameTree;

public class KuhnGameTree implements GameTree<String> {
    private String history = "";
    private int[] cards;

    public KuhnGameTree(int[] cards) {
        this.cards = cards;
    }

    private KuhnGameTree(KuhnGameTree copy) {
        this.history = copy.history;
        this.cards = copy.cards;
    }

    @Override
    public boolean isTerminalForPlayer(int playerId) {
        return history.endsWith("p") || history.endsWith("bb");
    }

    @Override
    public int getPayoffForPlayer(int playerId) {
        int playerCard = cards[playerId];
        int opponentCard = cards[(playerId + 1) % 2];
        if (history.endsWith("bb")) {
            return playerCard > opponentCard ? 200 : -200;
        }
        if (history.endsWith("pp")) {
            return playerCard > opponentCard ? 100  : -100;
        }
        if (history.endsWith("p")) {
            return isCurrentPlayer(playerId) ? 1 : -1;
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean isCurrentPlayer(int playerId) {
        return history.length() % 2 == playerId;
    }

    @Override
    public String asInfoSet(int playerId) {
        return cards[playerId] + history;
    }

    @Override
    public int actions() {
        return 3;
    }

    @Override
    public GameTree takeAction(int actionId) {
        KuhnGameTree next = new KuhnGameTree(this);
        next.history += switch (actionId) {
            case 0 -> "p";
            case 1 -> "b";
            case 2 -> "B";
            default -> throw new IllegalArgumentException();
        };
        return next;
    }

    @Override
    public boolean shouldUpdateRegrets() {
        return true;
    }
}
