package de.poker.solver.map.persistence;

import de.poker.solver.game.Action;
import de.poker.solver.map.Node;

import java.util.Map;

public interface ActionMapInterface {
    Map<Action, Node> getMap();
    void setMap(Map<Action, Node> map);
    void add(ActionMapInterface toPersist);
}
