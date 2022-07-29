package de.poker.solver.map.persistence;

import de.poker.solver.game.Action;
import de.poker.solver.map.ActionMap;
import de.poker.solver.map.Node;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.util.ReadResolvable;
import net.openhft.chronicle.hash.serialization.BytesReader;
import net.openhft.chronicle.hash.serialization.BytesWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ActionMapMarshaller implements BytesWriter<ActionMapInterface>, BytesReader<ActionMapInterface>, ReadResolvable<ActionMapMarshaller> {
    public static final ActionMapMarshaller INSTANCE = new ActionMapMarshaller();

    private ActionMapMarshaller() {}

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
    public ActionMapMarshaller readResolve() {
        return INSTANCE;
    }
}
