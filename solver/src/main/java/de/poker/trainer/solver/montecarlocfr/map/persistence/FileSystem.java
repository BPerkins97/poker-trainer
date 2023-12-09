package de.poker.trainer.solver.montecarlocfr.map.persistence;

import de.poker.trainer.solver.montecarlocfr.game.Card;
import de.poker.trainer.solver.montecarlocfr.game.Constants;
import de.poker.trainer.solver.montecarlocfr.map.ActionMap;
import de.poker.trainer.solver.montecarlocfr.map.InfoSet;
import de.poker.trainer.solver.montecarlocfr.utility.CardInfoSetBuilder;

import java.util.*;

public class FileSystem {
    private static Map<String, Map<InfoSet, ActionMap>>[] MAP = new Map[Constants.NUM_PLAYERS];

    static {
        for (int k=0;k<Constants.NUM_PLAYERS;k++) {
            MAP[k] = new HashMap<>();
        }
        for (int i=0;i<Card.NUM_CARDS;i++) {
            for (int j=i+1;j<Card.NUM_CARDS;j++) {
                for (int k=0;k<Constants.NUM_PLAYERS;k++) {
                    List<Card> cards = CardInfoSetBuilder.toNormalizedList(Arrays.asList(Card.of(i), Card.of(j)));
                    String key = cards.get(0).toString() + cards.get(1).toString();
                    MAP[k].put(key, new HashMap<>());
                }
            }
        }
    }

    private FileSystem() {}

    private static Map<InfoSet, ActionMap> getMap(InfoSet key) {
        return MAP[key.player()].get(key.holeCards());
    }

    public static void update(InfoSet key, ActionMap toPersist) {
        Map<InfoSet, ActionMap> map = getMap(key);
        synchronized (map) {
            ActionMap persisted = map.get(key);
            toPersist.add(persisted);
            map.put(key, toPersist);
        }
    }

    public synchronized static ActionMap getActionMap(InfoSet key) {
        Map<InfoSet, ActionMap> map = getMap(key);
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
    }
}
