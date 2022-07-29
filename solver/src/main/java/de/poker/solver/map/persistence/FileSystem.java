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
    private static ChronicleMap<InfoSet, ActionMap> MAP;

    static {
        try {
            MAP = ChronicleMapBuilder
                    .of(InfoSet.class, ActionMap.class)
                    .name("poker-trainer-map")
                    .averageKeySize(200)
                    .averageValueSize(80)
                    .entries(1_000_000_000L)
                    .createPersistedTo(new File("/root/prod.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileSystem() {}

    public synchronized static ActionMap get(InfoSet key) {
        return MAP.get(key);
    }

    public synchronized static void update(InfoSet key, ActionMap toPersist) {
        ActionMap persisted = MAP.get(key);
        toPersist.add(persisted);
        MAP.put(key, toPersist);
    }

    public synchronized static ActionMap getActionMap(InfoSet key) {
        ActionMap actionMap = get(key);
        if (Objects.isNull(actionMap)) {
            actionMap = new ActionMap();
            actionMap.setMap(new HashMap<>());
            MAP.putIfAbsent(key, actionMap);
        }
        return actionMap;
    }

    public synchronized static void close() {
        if (MAP.isOpen()) {
            System.out.println("Closing file system");
            MAP.close();
        }
    }
}
