package de.poker.solver.map;

import de.poker.solver.TestBytes;
import de.poker.solver.game.Action;
import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.OnHeapBytes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ActionMapMarshallerTest {

    @Test
    public void test2() {
        ActionMap input = new ActionMap();
        Map<Action, Node> actionNodeMap = new HashMap<>();
        actionNodeMap.put(Action.call(), new Node(50, (short)110));
        input.setMap(actionNodeMap);
        OnHeapBytes bytes = Bytes.allocateElasticOnHeap(100);
        input.writeMarshallable(bytes);
        System.out.println(bytes);
    }

    @Test
    public void test1() {
        ActionMap input = new ActionMap();
        Map<Action, Node> actionNodeMap = new HashMap<>();
        actionNodeMap.put(Action.call(), new Node(50, (short)110));
        input.setMap(actionNodeMap);
        test(input);
    }

    private void test(ActionMap input) {
        TestBytes bytes = new TestBytes();
        input.writeMarshallable(bytes);
        ActionMap output = new ActionMap();
        output.readMarshallable(bytes);
        Assertions.assertEquals(input, output);
    }
}
