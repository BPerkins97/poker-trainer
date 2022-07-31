package de.poker.solver.map.persistence;

import de.poker.solver.game.Constants;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.map.ActionMap;
import de.poker.solver.map.InfoSet;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class FileSystem {
    private static ChronicleMap<InfoSet, ActionMap>[] MAP = new ChronicleMap[Constants.NUM_PLAYERS];

    static {
        File file = new File("root/poker");
        file.mkdir();
        try {
            for (int i=0;i<Constants.NUM_PLAYERS;i++) {
                MAP[i] = ChronicleMapBuilder
                        .of(InfoSet.class, ActionMap.class)
                        .name("poker-trainer-map")
                        .averageKeySize(200)
                        .averageValueSize(80)
                        .entries(100_000_000L)
                        .createPersistedTo(new File("/root/poker/tst" + i + " .txt"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileSystem() {}

    public static void update(InfoSet key, ActionMap toPersist) {
        synchronized (MAP[key.player()]) {
            ActionMap persisted = MAP[key.player()].get(key);
            toPersist.add(persisted);
            MAP[key.player()].put(key, toPersist);
        }
    }

    public synchronized static ActionMap getActionMap(InfoSet key) {
        synchronized (MAP[key.player()]) {
            ActionMap actionMap = MAP[key.player()].get(key);
            if (Objects.isNull(actionMap)) {
                actionMap = new ActionMap();
                actionMap.setMap(new HashMap<>());
                MAP[key.player()].putIfAbsent(key, actionMap);
            }
            return actionMap;
        }
    }

    public synchronized static void close() {
        for (int i=0;i<Constants.NUM_PLAYERS;i++)
        if (MAP[i].isOpen()) {
            MAP[i].close();
            System.out.println("Closed file for player " + i);
        }
    }
}
