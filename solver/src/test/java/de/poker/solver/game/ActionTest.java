package de.poker.solver.game;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ActionTest {

    @Test
    public void testEquals() {
        Assertions.assertEquals(Action.call(), Action.call());
        Assertions.assertEquals(Action.fold(), Action.fold());
        Assertions.assertEquals(Action.raise(100), Action.raise(100));
        Assertions.assertNotEquals(Action.raise(1000), Action.raise(100));
        Assertions.assertNotEquals(Action.raise(1000), Action.fold());
        Assertions.assertNotEquals(Action.raise(1000), Action.call());
        Assertions.assertNotEquals(Action.fold(), Action.call());
    }

    @Test
    public void testHashCode() {
        Assertions.assertEquals(Action.call().hashCode(), Action.call().hashCode());
        Assertions.assertEquals(Action.fold().hashCode(), Action.fold().hashCode());
        Assertions.assertEquals(Action.raise(100).hashCode(), Action.raise(100).hashCode());
        Assertions.assertNotEquals(Action.raise(10).hashCode(), Action.raise(100).hashCode());
        Assertions.assertNotEquals(Action.raise(10).hashCode(), Action.fold().hashCode());
        Assertions.assertNotEquals(Action.raise(10).hashCode(), Action.call().hashCode());
        Assertions.assertNotEquals(Action.fold().hashCode(), Action.call().hashCode());
    }

    @Test
    public void testAmount() {
        Assertions.assertEquals(100, Action.raise(100).amount());
        Assertions.assertEquals(50, Action.raise(50).amount());
        Assertions.assertEquals(13, Action.raise(13).amount());
    }

    @Test
    public void amountHasToBeGreaterThan0() {
        try {
            Action.raise(0);
            Assertions.fail();
        } catch (AssertionError e) {
            // success
        }
        try {
            Action.raise(-1);
            Assertions.fail();
        } catch (AssertionError e) {
            // success
        }
        try {
            Action.raise(-100);
            Assertions.fail();
        } catch (AssertionError e) {
            // success
        }
    }

    @Test
    public void testIsCall() {
        Assertions.assertTrue(Action.call().isCall());
        Assertions.assertFalse(Action.fold().isCall());
        Assertions.assertFalse(Action.raise(10).isCall());
    }

    @Test
    public void testIsFold() {
        Assertions.assertTrue(Action.fold().isFold());
        Assertions.assertFalse(Action.call().isFold());
        Assertions.assertFalse(Action.raise(101).isFold());
    }
}
