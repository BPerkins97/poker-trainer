package de.poker.solver.map.persistence;

import de.poker.solver.game.Action;
import de.poker.solver.map.*;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class FileSystem {
    private static ChronicleMap<InfoSetInterface, ActionMapInterface> MAP;

    static {
        InfoSet averageKey = new InfoSet();
        averageKey.setCards(new byte[]{52,41,37,13,15,1,5});
        averageKey.setHistory(new byte[]{1, 2, 4, 5, 4, 10, 1, 1, 1, 2, 2, 4, -50, 4, 50, 4, -50});

        HashMap<Action, Node> averageMap = new HashMap<>();
        averageMap.put(Action.fold(), new Node(0, (short) 0));
        averageMap.put(Action.call(), new Node(0, (short) 0));
        averageMap.put(Action.raise(50), new Node(0, (short) 0));
        averageMap.put(Action.raise(100), new Node(0, (short) 0));
        averageMap.put(Action.raise(150), new Node(0, (short) 0));
        ActionMap averageValue = new ActionMap();
        averageValue.setMap(averageMap);
        try {
            MAP = ChronicleMapBuilder
                    .of(InfoSetInterface.class, ActionMapInterface.class)
                    .name("poker-trainer-map")
                    .averageKey(averageKey)
                    .entries(1_000_000)
                    .averageValue(averageValue)
                    .keyMarshaller(InfoSetMarshaller.INSTANCE)
                    .valueMarshallers(ActionMapMarshaller.INSTANCE, ActionMapMarshaller.INSTANCE)
                    .createPersistedTo(new File("C:/Temp/tst.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileSystem() {}


    public synchronized static ActionMapInterface get(InfoSetInterface key) {
        return MAP.get(key);
    }

    public synchronized static void update(InfoSetInterface key, ActionMapInterface toPersist) {
        ActionMapInterface persisted = MAP.get(key);
        toPersist.add(persisted);
        MAP.replace(key, persisted, toPersist);
    }

    public synchronized static ActionMap getActionMap(InfoSetInterface key) {
        ActionMapInterface actionMap = get(key);
        if (Objects.isNull(actionMap)) {
            actionMap = new ActionMap();
            actionMap.setMap(new HashMap<>());
            MAP.putIfAbsent(key, actionMap);
        }
        return (ActionMap) actionMap;
    }
}
