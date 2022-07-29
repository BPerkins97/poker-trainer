package de.poker.solver.map;

import de.poker.solver.game.Action;
import de.poker.solver.map.persistence.InfoSetInterface;

import java.util.Arrays;

public class InfoSet implements InfoSetInterface {
    private byte[] cards;
    private byte[] history;

    @Override
    public byte[] getCards() {
        return cards;
    }

    @Override
    public void setCards(byte[] cards) {
        this.cards = cards;
    }

    @Override
    public byte[] getHistory() {
        return history;
    }

    @Override
    public void setHistory(byte[] action) {
        this.history = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfoSet infoSet = (InfoSet) o;
        return Arrays.equals(cards, infoSet.cards) && Arrays.equals(history, infoSet.history);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(cards);
        result = 31 * result + Arrays.hashCode(history);
        return result;
    }

    @Override
    public String toString() {
        return "InfoSet{" +
                "cards=" + Arrays.toString(cards) +
                ", history=" + Arrays.toString(history) +
                '}';
    }
}
