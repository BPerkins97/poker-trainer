package de.poker.solver.map;

import de.poker.solver.game.Action;
import de.poker.solver.map.persistence.InfoSetInterface;

public class InfoSet implements InfoSetInterface {
    private byte[] cards;
    private Action[] history;

    @Override
    public byte[] getCards() {
        return cards;
    }

    @Override
    public void setCards(byte[] cards) {
        this.cards = cards;
    }

    @Override
    public Action[] getHistory() {
        return history;
    }

    @Override
    public void setHistory(Action[] action) {
        this.history = action;
    }
}
