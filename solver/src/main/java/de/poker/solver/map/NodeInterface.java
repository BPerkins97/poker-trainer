package de.poker.solver.map;

import de.poker.solver.game.Action;

import java.util.Map;

public interface NodeInterface {
    int getRegret();
    void setRegret(int regret);

    short getAverageAction();
    void setAverageAction(short averageAction);
}
