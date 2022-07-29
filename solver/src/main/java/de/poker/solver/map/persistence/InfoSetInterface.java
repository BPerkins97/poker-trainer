package de.poker.solver.map.persistence;

public interface InfoSetInterface {
    byte[] getCards();
    void setCards(byte[] cards);

    byte[] getHistory();
    void setHistory(byte[] action);
}
