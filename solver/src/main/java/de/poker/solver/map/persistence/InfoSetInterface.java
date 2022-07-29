package de.poker.solver.map.persistence;

import de.poker.solver.game.Action;

public interface InfoSetInterface {
    byte[] getCards();
    void setCards(byte[] cards);

    Action[] getHistory();
    void setHistory(Action[] action);
}
