package de.poker.solver.map;

import de.poker.solver.TestBytes;
import de.poker.solver.game.Action;
import de.poker.solver.map.persistence.InfoSetMarshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InfoSetMarshallerTest {

    @Test
    public void test1() {
        InfoSet input = new InfoSet();
        input.setHistory(new Action[]{Action.fold(), Action.call(), Action.raise(100)});
        input.setCards(new byte[]{51, 39});
        test(input);
    }

    private void test(InfoSet input) {
        InfoSetMarshaller instance = InfoSetMarshaller.INSTANCE;
        TestBytes bytes = new TestBytes();
        instance.write(bytes, input);
        Assertions.assertEquals(input, instance.read(bytes, null));
    }
}
