package pcm.state.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pcm.controller.Player;
import pcm.util.TestUtils;
import teaselib.Body;
import teaselib.State;
import teaselib.core.TeaseLib;

public class SetStateTest {

    @Test
    public void testSetState() throws Exception {
        Player player = TestUtils.createPlayer(SetStateTest.class);
        State state = player.state(Body.SomethingOnPenis);

        StateCommand foo = new StateCommand(new String[] { "Apply", "teaselib.Body.SomethingOnPenis",
                "teaselib.Toys.Chastity_Device" });
        foo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());

        StateCommand rememberFoo = new StateCommand(new String[] { "Apply",
                "teaselib.Body.SomethingOnPenis", "teaselib.Toys.Chastity_Device", "Remember" });
        rememberFoo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());

        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(
                TeaseLib.DefaultDomain, Body.class.getName(),
                Body.SomethingOnPenis.name() + ".state.duration");

        assertTrue(stateStorage.available());

        StateCommand clearFoo = new StateCommand(
                new String[] { "Remove", "teaselib.Body.SomethingOnPenis" });
        clearFoo.execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(state.applied());
        assertTrue(state.expired());

        StateCommand fooTimed = new StateCommand(new String[] { "Apply", "teaselib.Body.SomethingOnPenis",
                "teaselib.Toys.Chastity_Device", "600" });
        fooTimed.execute(player.state);

        assertTrue(state.applied());
        assertFalse(state.expired());
        assertEquals(600, state.duration().limit(TimeUnit.MINUTES));
    }

    @Test
    public void testSetStateIndefinitely() throws Exception {
        Player player = TestUtils.createPlayer(SetStateTest.class);
        State state = player.state(Body.SomethingOnPenis);

        new StateCommand(new String[] { "Apply", "teaselib.Body.SomethingOnPenis",
                "teaselib.Toys.Chastity_Device", "inf" }).execute(player.state);

        assertTrue(state.applied());
        assertFalse(state.expired());
        assertTrue(state.duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        new StateCommand(new String[] { "Remove", "teaselib.Body.SomethingOnPenis" })
                .execute(player.state);

        assertFalse(state.applied());
        assertTrue(state.expired());
    }

    @Test
    public void testSetStateIndefinitelyAndRemember() throws Exception {
        Player player = TestUtils.createPlayer(SetStateTest.class);
        State state = player.state(Body.SomethingOnPenis);
        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(
                TeaseLib.DefaultDomain, Body.class.getName(),
                Body.SomethingOnPenis.name() + ".state.duration");

        new StateCommand(new String[] { "Apply", "teaselib.Body.SomethingOnPenis",
                "teaselib.Toys.Chastity_Device", "inf", "remember" }).execute(player.state);

        assertTrue(stateStorage.available());
        assertTrue(state.applied());
        assertFalse(state.expired());
        assertTrue(state.duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        new StateCommand(new String[] { "Remove", "teaselib.Body.SomethingOnPenis" })
                .execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(state.applied());
        assertTrue(state.expired());

    }
}
