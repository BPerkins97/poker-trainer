package de.poker.solver.cfr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.poker.solver.cfr.GameTreeNode.Type.EVERYONE_FOLDED;
import static de.poker.solver.cfr.GameTreeNode.Type.SHOWDOWN;

public class GameTreeNode {
    private Type type;
    private List<GameTreeNode> children;
    private double[] regretSum;
    private double[] strategySum;
    private int numActions;
    private double amount = 0;

    public GameTreeNode(Type type, List<GameTreeNode> children, double amount) {
        this.type = type;
        this.children = children;
        this.amount = amount;
    }

    public Type type() {
        return type;
    }

    public int countLeafes() {
        if (type == SHOWDOWN || type == EVERYONE_FOLDED) {
            return 1;
        }
        return children
                .stream()
                .map(GameTreeNode::countLeafes)
                .reduce(Integer::sum)
                .orElseThrow();
    }

    public String asString(String indentation) {
        String str = indentation + type;
        if (amount > 0) {
            str += " " + amount;
        }
        String childrenStrs = children
                .stream()
                .map(c -> c.asString(indentation + "  "))
                .collect(Collectors.joining());
        return str + "\n" + childrenStrs;
    }
    public enum Type {
        DEAL_HOLE_CARDS,
        FOLD,
        CALL,
        RAISE,
        SHOWDOWN,
        EVERYONE_FOLDED,
        DEAL_FLOP,
        DEAL_TURN,
        DEAL_RIVER
    }
}
