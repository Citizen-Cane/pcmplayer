package pcm.state.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pcm.controller.Declarations;
import pcm.controller.Player;
import pcm.controller.StateCommandLineParameters;
import pcm.util.TestUtils;
import teaselib.Body;
import teaselib.State;
import teaselib.core.TeaseLib;

public class StateCommandTest {

    @Test
    public void testSetState() throws Exception {
        Player player = TestUtils.createPlayer(StateCommandTest.class);
        State state = player.state(Body.SomethingOnPenis);
        Declarations declarations = new Declarations();

        StateCommand foo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Body.SomethingOnPenis", "Apply", "teaselib.Toys.Chastity_Device" },
                declarations));
        foo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());

        StateCommand rememberFoo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Body.SomethingOnPenis", "Apply", "teaselib.Toys.Chastity_Device", "Remember" },
                declarations));
        rememberFoo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());

        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                Body.class.getName(), Body.SomethingOnPenis.name() + ".state.duration");

        assertTrue(stateStorage.available());

        StateCommand clearFoo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Body.SomethingOnPenis", "Remove" }, declarations));
        clearFoo.execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(state.applied());
        assertTrue(state.expired());

        StateCommand fooTimed = new StateCommand(
                new StateCommandLineParameters(new String[] { "teaselib.Body.SomethingOnPenis", "Apply",
                        "teaselib.Toys.Chastity_Device", "over", "10:00\"00" }, declarations));
        fooTimed.execute(player.state);

        assertTrue(state.applied());
        assertFalse(state.expired());
        assertEquals(10, state.duration().limit(TimeUnit.HOURS));
    }

    @Test
    public void testSetStateIndefinitely() throws Exception {
        Player player = TestUtils.createPlayer(StateCommandTest.class);
        State state = player.state(Body.SomethingOnPenis);
        Declarations declarations = new Declarations();

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Body.SomethingOnPenis", "Apply",
                "teaselib.Toys.Chastity_Device", "over", "inf" }, declarations)).execute(player.state);

        assertTrue(state.applied());
        assertFalse(state.expired());
        assertTrue(state.duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Body.SomethingOnPenis", "Remove" },
                declarations)).execute(player.state);

        assertFalse(state.applied());
        assertTrue(state.expired());
    }

    @Test
    public void testSetStateIndefinitelyAndRemember() throws Exception {
        Player player = TestUtils.createPlayer(StateCommandTest.class);
        State state = player.state(Body.SomethingOnPenis);
        Declarations declarations = new Declarations();

        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                Body.class.getName(), Body.SomethingOnPenis.name() + ".state.duration");

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Body.SomethingOnPenis", "Apply",
                "teaselib.Toys.Chastity_Device", "over", "inf", "remember" }, declarations)).execute(player.state);

        assertTrue(stateStorage.available());
        assertTrue(state.applied());
        assertFalse(state.expired());
        assertTrue(state.duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Body.SomethingOnPenis", "Remove" },
                declarations)).execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(state.applied());
        assertTrue(state.expired());
    }
}
