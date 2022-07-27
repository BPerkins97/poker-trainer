package de.poker.solver.database;

import de.poker.solver.game.Action;
import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.map.ActionMap;
import de.poker.solver.map.ActionMapInterface;
import de.poker.solver.map.Marshaller;
import de.poker.solver.map.Node;
import net.openhft.chronicle.bytes.BytesUtil;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class FileSystem {
    private static ChronicleMap<CharSequence, ActionMapInterface> MAP;
    private static final FileSystem INSTANCE = new FileSystem();

    public static void generate() {
        HashMap<Action, Node> averageMap = new HashMap<>();
        averageMap.put(Action.fold(), new Node(0, (short) 0));
        averageMap.put(Action.call(), new Node(0, (short) 0));
        averageMap.put(Action.raise(50), new Node(0, (short) 0));
        averageMap.put(Action.raise(100), new Node(0, (short) 0));
        averageMap.put(Action.raise(150), new Node(0, (short) 0));
        ActionMap actionMap = new ActionMap();
        actionMap.setMap(averageMap);
        try {
            MAP = ChronicleMapBuilder
                    .of(CharSequence.class, ActionMapInterface.class)
                    .name("poker-trainer-map")
                    .averageKey("AcKcQcJcTc9c8ccccccccccccccccccccccccccccccccccccr100r100r100r100r100r100")
                    .entries(1_000_000_000)
                    .averageValue(actionMap)
                    .valueMarshallers(Marshaller.INSTANCE, Marshaller.INSTANCE)
                    .createPersistedTo(new File("/root/tst.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static FileSystem getInstance() {
        return INSTANCE;
    }

    public ActionMapInterface get(String key) {
        return MAP.get(key);
    }

    public void update(String key, ActionMapInterface actionMap) {
        MAP.replace(key, actionMap);
    }

    public ActionMap getActionMap(HoldEmGameTree state) {
        ChronicleMap<CharSequence, ActionMapInterface> map = MAP;
        String key = state.cardInfoSet(state.bettingRound, state.currentPlayer) + state.history();
        ActionMapInterface actionMap = get(key);
        if (Objects.isNull(actionMap)) {
            actionMap = new ActionMap();
            actionMap.setMap(new HashMap<>());
            MAP.putIfAbsent(key, actionMap);
        }
        return (ActionMap) actionMap;
    }
}
