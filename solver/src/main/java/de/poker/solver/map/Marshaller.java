package de.poker.solver.map;

import de.poker.solver.game.Action;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.util.ReadResolvable;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Marshaller implements BytesWriter<ActionMapInterface>, BytesReader<ActionMapInterface>, ReadResolvable<Marshaller> {
    public static final Marshaller INSTANCE = new Marshaller();

    private Marshaller() {}

    @Override
    public void write(Bytes out, ActionMapInterface toWrite) {
        Map<Action, Node> map = toWrite.getMap();
        if (Objects.isNull(map)) {
            out.writeByte((byte) 0);
        } else {
            byte size = (byte) map.size();
            out.writeByte(size);
            toWrite.getMap().forEach((action, node) -> {
                out.writeByte(action.type());
                out.writeShort(action.amount());
                out.writeInt(node.getRegret());
                out.writeShort(node.getAverageAction());
            });
        }
    }

    @Override
    public ActionMapInterface read(Bytes in, ActionMapInterface using) {
        if (Objects.isNull(using)) {
            using = new ActionMap();
        }
        byte numAction = in.rawReadByte();
        Map<Action, Node> map = using.getMap();
        if (Objects.isNull(map)) {
            map = new HashMap<>(numAction, 1);
            using.setMap(map);
        }
        for (int i=0;i<numAction;i++) {
            byte actionType = in.readByte();
            short amount = in.readShort();
            Action action = Action.of(actionType, amount);
            int regret = in.readInt();
            short averageAction =  in.readShort();
            Node node = new Node(regret, averageAction);
            map.put(action, node);
        }
        return using;
    }

    @Override
    public Marshaller readResolve() {
        return INSTANCE;
    }
}
