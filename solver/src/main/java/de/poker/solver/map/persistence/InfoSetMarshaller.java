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

        int numActions = in.readByte() + Byte.MAX_VALUE;
        Action[] history = using.getHistory();
        if (Objects.isNull(history) || history.length != numActions) {
            history = new Action[numActions];
        }
        for (int i=0;i<numActions;i++) {
            byte type = in.readByte();
            int amount = 0;
            if (type == RAISE) {
                amount = in.readByte() + Byte.MAX_VALUE;
            }
            history[i] = Action.of(type, amount);
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

        Action[] history = toWrite.getHistory();
        int numActions = 0;
        if (!Objects.isNull(history)) {
            numActions = history.length;
            out.writeByte((byte)(history.length - Byte.MAX_VALUE));
        }
        for (int i=0;i<numActions;i++) {
            byte type = history[i].type();
            out.writeByte(type);
            if (type == RAISE) {
                int amount = history[i].amount() - Byte.MAX_VALUE;
                out.writeByte((byte)amount);
            }
        }
    }
}
