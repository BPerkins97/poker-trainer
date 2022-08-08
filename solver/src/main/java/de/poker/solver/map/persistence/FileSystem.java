package de.poker.solver.map.persistence;

import de.poker.solver.game.Card;
import de.poker.solver.game.Constants;
import de.poker.solver.map.ActionMap;
import de.poker.solver.utility.CardInfoSetBuilder;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileSystem {
    private static Map<String, ChronicleMap<InfoSet, ActionMap>>[] MAP = new Map[Constants.NUM_PLAYERS];

    static {
        for (int k=0;k<Constants.NUM_PLAYERS;k++) {
            MAP[k] = new HashMap<>();
        }
        for (int i=0;i<Card.NUM_CARDS;i++) {
            for (int j=i+1;j<Card.NUM_CARDS;j++) {
                for (int k=0;k<Constants.NUM_PLAYERS;k++) {
                    List<Card> cards = CardInfoSetBuilder.toNormalizedList(Arrays.asList(Card.of(i), Card.of(j)));
                    String key = cards.get(0).toString() + cards.get(1).toString();
                    String pathName = "/root/poker/player" + k;
                    String fileName = pathName + "/" + key + ".txt";
                    File file = new File(fileName);
                    File directory = new File(pathName);
                    if (file.exists()) {
                        break;
                    }

                    directory.mkdirs();
                    try {
                        ChronicleMap<InfoSet, ActionMap> persistedTo = ChronicleMapBuilder
                                .of(InfoSet.class, ActionMap.class)
                                .name("poker-trainer-map-" + k + "-" + key)
                                .averageKeySize(200)
                                .averageValueSize(80)
                                .entries(1_000_000L)
                                .createPersistedTo(file);
                        MAP[k].put(key, persistedTo);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private FileSystem() {}

    private static ChronicleMap<InfoSet, ActionMap> getMap(InfoSet key) {
        return MAP[key.player()].get(key.holeCards());
    }

    public static void update(InfoSet key, ActionMap toPersist) {
        ChronicleMap<InfoSet, ActionMap> map = getMap(key);
        synchronized (map) {
            ActionMap persisted = map.get(key);
            toPersist.add(persisted);
            map.put(key, toPersist);
        }
    }

    public synchronized static ActionMap getActionMap(InfoSet key) {
        ChronicleMap<InfoSet, ActionMap> map = getMap(key);
        synchronized (map) {
            ActionMap actionMap = map.get(key);
            if (Objects.isNull(actionMap)) {
                actionMap = new ActionMap();
                actionMap.setMap(new HashMap<>());
                map.putIfAbsent(key, actionMap);
            }
            return actionMap;
        }
    }

    public synchronized static void close() {
        for (int i=0;i<Constants.NUM_PLAYERS;i++) {
            for (ChronicleMap<InfoSet, ActionMap> value : MAP[i].values()) {
                if (value.isOpen()) {
                    value.close();
                }
            }
            System.out.println("Closed files for player " + i);
        }
    }
}
