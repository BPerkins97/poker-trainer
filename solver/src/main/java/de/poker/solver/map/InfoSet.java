package de.poker.solver.map;

import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesMarshallable;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.Objects;

public class InfoSet implements BytesMarshallable {
    private byte player;
    private byte[] cards;
    private byte[] history;

    public byte[] cards() {
        return cards;
    }

    public void cards(byte[] cards) {
        this.cards = cards;
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
    public void readMarshallable(BytesIn in) throws IORuntimeException, BufferUnderflowException, IllegalStateException {
        byte numCards = in.readByte();
        if (Objects.isNull(cards) || cards.length != numCards) {
            cards = new byte[numCards];
        }
        for (int i=0;i<numCards;i++) {
            cards[i] = in.readByte();
        }

        int numActionBytes = in.readShort();
        if (Objects.isNull(history) || history.length != numActionBytes) {
            history = new byte[numActionBytes];
        }
        for (int i=0;i<numActionBytes;i++) {
            history[i] = in.readByte();
        }
    }

    @Override
    public void writeMarshallable(BytesOut out) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, ArithmeticException {
        int numCards = cards.length;
        out.writeByte((byte) numCards);
        for (int i=0;i<numCards;i++) {
            out.writeByte(cards[i]);
        }

        short numActionBytes = 0;
        if (!Objects.isNull(history)) {
            numActionBytes = (short) history.length;
            out.writeByte((byte)(history.length - Byte.MAX_VALUE));
        }
        assert numActionBytes >= 0;
        for (int i=0;i<numActionBytes;i++) {
            out.writeByte(history[i]);
        }
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
