package de.poker.solver.map;

import de.poker.solver.game.Action;

import java.util.Map;

public interface ActionMapInterface {
    Map<Action, Node> getMap();
    void setMap(Map<Action, Node> map);
}
