package de.poker.solver.map;

import de.poker.solver.TestBytes;
import de.poker.solver.game.Action;
import jnr.ffi.annotations.In;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InfoSetMarshallerTest {

    @Test
    public void test1() {
        InfoSet input = new InfoSet();
        input.history(new byte[]{Action.fold().type(), Action.call().type(), Action.raise(100).type(), (byte)Action.raise(100).amount()});
        input.cards(new byte[]{51, 39});
        test(input);
    }

    private void test(InfoSet input) {
        TestBytes bytes = new TestBytes();
        input.writeMarshallable(bytes);
        InfoSet output = new InfoSet();
        output.readMarshallable(bytes);
        Assertions.assertEquals(input, output);
    }
}
