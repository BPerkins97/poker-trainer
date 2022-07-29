package de.poker.solver.map.persistence;

import de.poker.solver.game.Action;
import de.poker.solver.map.InfoSet;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.util.ReadResolvable;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;

import java.util.Objects;

import static de.poker.solver.game.Action.RAISE;

public class InfoSetMarshaller implements BytesWriter<InfoSetInterface>, BytesReader<InfoSetInterface>, ReadResolvable<InfoSetMarshaller> {
    public static final InfoSetMarshaller INSTANCE = new InfoSetMarshaller();

    private InfoSetMarshaller() {}

    @Override
    public InfoSetMarshaller readResolve() {
        return INSTANCE;
    }

    @Override
    public InfoSetInterface read(Bytes in, InfoSetInterface using) {
        if (Objects.isNull(using)) {
            using = new InfoSet();
        }
        byte numCards = in.readByte();
        byte[] cards = using.getCards();
        if (Objects.isNull(cards) || cards.length != numCards) {
            cards = new byte[numCards];
        }
        for (int i=0;i<numCards;i++) {
            cards[i] = in.readByte();
        }
        using.setCards(cards);

        int numActionBytes = in.readByte() + Byte.MAX_VALUE;
        byte[] history = using.getHistory();
        if (Objects.isNull(history) || history.length != numActionBytes) {
            history = new byte[numActionBytes];
        }
        for (int i=0;i<numActionBytes;i++) {
            history[i] = in.readByte();
        }
        using.setHistory(history);
        return using;
    }

    @Override
    public void write(Bytes out, InfoSetInterface toWrite) {
        byte[] cards = toWrite.getCards();
        int numCards = cards.length;
        out.writeByte((byte) numCards);
        for (int i=0;i<numCards;i++) {
            out.writeByte(cards[i]);
        }

        byte[] history = toWrite.getHistory();
        int numActionBytes = 0;
        if (!Objects.isNull(history)) {
            numActionBytes = history.length;
            out.writeByte((byte)(history.length - Byte.MAX_VALUE));
        }
        for (int i=0;i<numActionBytes;i++) {
            out.writeByte(history[i]);
        }
    }
}
