package de.poker.solver.map;

import de.poker.solver.TestBytes;
import de.poker.solver.game.Action;
import de.poker.solver.map.persistence.ActionMapMarshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ActionMapMarshallerTest {

    @Test
    public void test1() {
        ActionMap input = new ActionMap();
        Map<Action, Node> actionNodeMap = new HashMap<>();
        actionNodeMap.put(Action.call(), new Node(50, (short)110));
        input.setMap(actionNodeMap);
        test(input);
    }

    private void test(ActionMap input) {
        ActionMapMarshaller instance = ActionMapMarshaller.INSTANCE;
        TestBytes bytes = new TestBytes();
        instance.write(bytes, input);
        Assertions.assertEquals(input, instance.read(bytes, null));
    }
}
