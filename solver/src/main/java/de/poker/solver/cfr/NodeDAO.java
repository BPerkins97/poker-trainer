package de.poker.solver.cfr;

import java.util.Optional;

public interface NodeDAO {
    public Optional<Node> findByInfoSet(String infoSet);

    void persist(String infoSet, Node node);
}
