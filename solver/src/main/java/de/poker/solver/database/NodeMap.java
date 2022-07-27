package de.poker.solver.database;

import de.poker.solver.game.HoldEmGameTree;
import de.poker.solver.map.ActionMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NodeMap {
    Map<InfoSet, ActionMap> map;

    public NodeMap(Map<InfoSet, ActionMap> map) {
        this.map = map;
    }

    public ActionMap getActionMap(HoldEmGameTree state) {
        InfoSet infoSet = state.toInfoSet();
        ActionMap actionMap = map.get(infoSet);
        if (Objects.isNull(actionMap)) {
            actionMap = new ActionMap();
            actionMap.setMap(new HashMap<>());
            map.put(infoSet, actionMap);
        }
        return actionMap;
    }
}
