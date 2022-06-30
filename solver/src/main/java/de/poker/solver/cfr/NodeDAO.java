package de.poker.solver.cfr;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface NodeDAO {
    public Optional<Node> findByInfoSet(String infoSet);

    void persist(String infoSet, Node node);

    Map<String, Node> findAll();

    List<String> findInfoSets();
}
