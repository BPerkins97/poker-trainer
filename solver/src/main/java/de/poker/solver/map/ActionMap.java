package de.poker.solver.map;

import de.poker.solver.game.Action;
import net.openhft.chronicle.bytes.BytesIn;
import net.openhft.chronicle.bytes.BytesMarshallable;
import net.openhft.chronicle.bytes.BytesOut;
import net.openhft.chronicle.core.io.IORuntimeException;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO https://www.nakivo.com/blog/how-to-use-remote-desktop-connection-ubuntu-linux-walkthrough/
public class ActionMap implements BytesMarshallable {
    private Map<Action, Node> map;

    public Strategy calculateStrategy(List<Action> actions) {
        double sum = 0;
        Strategy strategy = new Strategy(actions);
        for (Action action : actions) {
            Node node = getNode(action);
            int max = Math.max(0, node.getRegret());
            strategy.probabilityFor(action, max);
            sum += max;
        }
        if (sum > 0) {
            strategy.normalize(sum);
        } else {
            strategy.evenlyDistributed();
        }
        return strategy;
    }

    private Node getNode(Action a) {
        Node node = map.get(a);
        if (Objects.isNull(node)) {
            node = new Node(0, (short) 0);
            map.put(a, node);
        }
        return node;
    }

    public void visitAction(Action action) {
        Node node = getNode(action);
        node.incrementAverageAction();
    }

    public void addRegretForAction(Action action, int regret) {
        Node node = getNode(action);
        node.addRegret(regret);
    }

    public boolean regretForActionisAboveLimit(Action action, int limit) {
        Node node = getNode(action);
        return node.getRegret() > limit;
    }

    public Map<Action, Node> getMap() {
        return map;
    }

    public void setMap(Map<Action, Node> map) {
        this.map = map;
    }

    public void add(ActionMap persisted) {
        map.forEach((key, value) -> {
            Node node = persisted.getMap().get(key);
            if (!Objects.isNull(node)) {
                value.add(node);
            }
        });
    }

    @Override
    public void readMarshallable(BytesIn in) throws IORuntimeException, BufferUnderflowException, IllegalStateException {
        byte numAction = in.readByte();
        if (Objects.isNull(map)) {
            map = new HashMap<>(numAction, 1);
        }
        for (int i=0;i<numAction;i++) {
            byte actionType = in.readByte();
            short amount = in.readShort();
            Action action = Action.of(actionType, amount);
            int regret = in.readInt();
            short averageAction =  in.readShort();
            Node node = new Node(regret, averageAction);
            map.put(action, node);
        }
    }

    @Override
    public void writeMarshallable(BytesOut out) throws IllegalStateException, BufferOverflowException, BufferUnderflowException, ArithmeticException {
        if (Objects.isNull(map)) {
            out.writeByte((byte) 0);
        } else {
            byte size = (byte) map.size();
            out.writeByte(size);
            map.forEach((action, node) -> {
                out.writeByte(action.type());
                out.writeShort(action.amount());
                out.writeInt(node.getRegret());
                out.writeShort(node.getAverageAction());
            });
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionMap actionMap = (ActionMap) o;
        return Objects.equals(map, actionMap.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return "ActionMap{" +
                "map=" + map +
                '}';
    }
}
