package de.poker.trainer.solver.montecarlocfr.map;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.Objects;

public class InfoSet {
    private byte player;
    private byte[] cards;
    private byte[] history;
    private String holeCards;

    public byte[] cards() {
        return cards;
    }

    public void cards(byte[] cards) {
        this.cards = cards;
    }

    public String holeCards() {
        return holeCards;
    }

    public void holeCards(String holeCards) {
        this.holeCards = holeCards;
    }

    public byte[] history() {
        return history;
    }

    public void history(byte[] action) {
        this.history = action;
    }

    public byte player() {
        return player;
    }

    public void player(byte player) {
        this.player = player;
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
