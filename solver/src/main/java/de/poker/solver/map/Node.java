package de.poker.solver.map;

import de.poker.solver.ApplicationConfiguration;
import net.openhft.chronicle.bytes.BytesMarshallable;

import java.util.Objects;

public class Node implements BytesMarshallable {
    int regret;
    int regretGrowth;
    short averageAction;
    byte numTouchedSinceLastDiscount;
    byte numDiscounted;

    public Node() {
    }

    public Node(int regret, short averageAction) {
        this.regret = Math.max(regret, ApplicationConfiguration.MINIMUM_REGRET);
        this.averageAction = averageAction;
    }

    public int getRegret() {
        return regret;
    }

    public void incrementAverageAction() {
        averageAction++;
    }

    public short getAverageAction() {
        return averageAction;
    }

    public void addRegret(int regret) {
        this.regretGrowth += regret;
    }

    public void add(Node node) {
        this.regret = node.regret + regretGrowth;
        this.averageAction += node.averageAction;
        if (node.numDiscounted < 100) {
            if (node.numTouchedSinceLastDiscount > 100) {
                this.numDiscounted = (byte)(node.numDiscounted + 1);
                this.numTouchedSinceLastDiscount = Byte.MIN_VALUE;
                this.regret *= (double) node.numDiscounted / this.numDiscounted;
            } else {
                this.numTouchedSinceLastDiscount = (byte) (node.numTouchedSinceLastDiscount + 1);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return regret == node.regret && regretGrowth == node.regretGrowth && averageAction == node.averageAction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(regret, regretGrowth, averageAction);
    }

    @Override
    public String toString() {
        return "Node{" +
                "regret=" + regret +
                ", regretGrowth=" + regretGrowth +
                ", averageAction=" + averageAction +
                '}';
    }

    public void makePersistable() {
        this.regret = regretGrowth;
        this.numTouchedSinceLastDiscount = Byte.MIN_VALUE;
    }
}
