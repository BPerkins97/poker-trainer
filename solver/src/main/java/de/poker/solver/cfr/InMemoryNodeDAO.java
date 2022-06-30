package de.poker.solver.cfr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryNodeDAO implements NodeDAO {
    private Map<String, Node> nodes = new HashMap<>();


    @Override
    public Optional<Node> findByInfoSet(String infoSet) {
        return Optional.ofNullable(nodes.get(infoSet));
    }

    @Override
    public void persist(String infoset, Node node) {
        nodes.put(infoset, node);
    }

    @Override
    public Map<String, Node> findAll() {
        return nodes;
    }

    @Override
    public List<String> findInfoSets() {
        return nodes.keySet().stream().toList();
    }
}
