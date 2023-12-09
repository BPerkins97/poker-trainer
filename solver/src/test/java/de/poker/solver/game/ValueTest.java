package de.poker.solver.game;

import de.poker.trainer.solver.montecarlocfr.game.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValueTest {
    @Test
    public void testOrdering() {
        Assertions.assertTrue(Value.ACE.compareTo(Value.KING) > 0);
        Assertions.assertTrue(Value.KING.compareTo(Value.QUEEN) > 0);
        Assertions.assertTrue(Value.QUEEN.compareTo(Value.JACK) > 0);
        Assertions.assertTrue(Value.JACK.compareTo(Value.TEN) > 0);
        Assertions.assertTrue(Value.TEN.compareTo(Value.NINE) > 0);
        Assertions.assertTrue(Value.NINE.compareTo(Value.EIGHT) > 0);
        Assertions.assertTrue(Value.EIGHT.compareTo(Value.SEVEN) > 0);
        Assertions.assertTrue(Value.SEVEN.compareTo(Value.SIX) > 0);
        Assertions.assertTrue(Value.SIX.compareTo(Value.FIVE) > 0);
        Assertions.assertTrue(Value.FIVE.compareTo(Value.FOUR) > 0);
        Assertions.assertTrue(Value.FOUR.compareTo(Value.THREE) > 0);
        Assertions.assertTrue(Value.THREE.compareTo(Value.TWO) > 0);
    }
    
    @Test
    public void testValue() {
        Assertions.assertEquals(0, Value.TWO.value());
        Assertions.assertEquals(1, Value.THREE.value());
        Assertions.assertEquals(2, Value.FOUR.value());
        Assertions.assertEquals(3, Value.FIVE.value());
        Assertions.assertEquals(4, Value.SIX.value());
        Assertions.assertEquals(5, Value.SEVEN.value());
        Assertions.assertEquals(6, Value.EIGHT.value());
        Assertions.assertEquals(7, Value.NINE.value());
        Assertions.assertEquals(8, Value.TEN.value());
        Assertions.assertEquals(9, Value.JACK.value());
        Assertions.assertEquals(10, Value.QUEEN.value());
        Assertions.assertEquals(11, Value.KING.value());
        Assertions.assertEquals(12, Value.ACE.value());
    }

    @Test
    public void testSymbol() {
        Assertions.assertEquals("2", Value.TWO.toString());
        Assertions.assertEquals("3", Value.THREE.toString());
        Assertions.assertEquals("4", Value.FOUR.toString());
        Assertions.assertEquals("5", Value.FIVE.toString());
        Assertions.assertEquals("6", Value.SIX.toString());
        Assertions.assertEquals("7", Value.SEVEN.toString());
        Assertions.assertEquals("8", Value.EIGHT.toString());
        Assertions.assertEquals("9", Value.NINE.toString());
        Assertions.assertEquals("T", Value.TEN.toString());
        Assertions.assertEquals("J", Value.JACK.toString());
        Assertions.assertEquals("Q", Value.QUEEN.toString());
        Assertions.assertEquals("K", Value.KING.toString());
        Assertions.assertEquals("A", Value.ACE.toString());
    }

    @Test
    public void testValuesInOrder() {
        Value[] values = Value.valuesInOrder();
        Value v = values[0];
        for (int i=1;i<13;i++) {
            Assertions.assertTrue(values[i].compareTo(v) > 0);
            v = values[i];
        }
    }

    @Test
    public void testNumValuesIs13() {
        Assertions.assertEquals(13, Value.NUM_VALUES);
        Assertions.assertEquals(13, Value.values().length);
        Assertions.assertEquals(13, Value.valuesInOrder().length);
    }

    @Test
    public void testParse() {
        Assertions.assertEquals(Value.ACE, Value.parse("A"));
        Assertions.assertEquals(Value.KING, Value.parse("K"));
        Assertions.assertEquals(Value.QUEEN, Value.parse("Q"));
        Assertions.assertEquals(Value.JACK, Value.parse("J"));
        Assertions.assertEquals(Value.TEN, Value.parse("T"));
        Assertions.assertEquals(Value.NINE, Value.parse("9"));
        Assertions.assertEquals(Value.EIGHT, Value.parse("8"));
        Assertions.assertEquals(Value.SEVEN, Value.parse("7"));
        Assertions.assertEquals(Value.SIX, Value.parse("6"));
        Assertions.assertEquals(Value.FIVE, Value.parse("5"));
        Assertions.assertEquals(Value.FOUR, Value.parse("4"));
        Assertions.assertEquals(Value.THREE, Value.parse("3"));
        Assertions.assertEquals(Value.TWO, Value.parse("2"));
        try {
            Value.parse("invalid");
            Assertions.fail();
        } catch (Exception e) {

        }
    }
}
