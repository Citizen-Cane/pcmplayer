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
import teaselib.Toys;
import teaselib.core.TeaseLib;

public class StateCommandTest {

    @Test
    public void testSetState() throws Exception {
        Player player = TestUtils.createPlayer(StateCommandTest.class, "../StateCommandTest");
        State state = player.state(Toys.Chastity_Device);
        Declarations declarations = new Declarations();
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STRING);

        StateCommand foo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "teaselib.Body.SomethingOnPenis" },
                declarations));
        foo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());
        assertTrue(state.is(player.namespaceApplyAttribute));
        assertTrue(state.is(player.script.scriptApplyAttribute));

        StateCommand rememberFoo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "teaselib.Body.SomethingOnPenis", "Remember" },
                declarations));
        rememberFoo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());

        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                Toys.class.getName(), Toys.Chastity_Device.name() + ".state.duration");

        assertTrue(stateStorage.available());

        StateCommand clearFoo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Remove" }, declarations));
        clearFoo.execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(state.applied());
        assertTrue(state.expired());

        StateCommand fooTimed = new StateCommand(
                new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Apply",
                        "teaselib.Body.SomethingOnPenis", "over", "10:00\"00" }, declarations));
        fooTimed.execute(player.state);

        assertTrue(state.applied());
        assertFalse(state.expired());
        assertEquals(10, state.duration().limit(TimeUnit.HOURS));
    }

    @Test
    public void testSetStateIndefinitely() throws Exception {
        Player player = TestUtils.createPlayer(StateCommandTest.class, "../StateCommandTest");
        State state = player.state(Toys.Chastity_Device);
        Declarations declarations = new Declarations();
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STRING);

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Apply",
                "teaselib.Body.SomethingOnPenis", "over", "inf" }, declarations)).execute(player.state);

        assertTrue(state.applied());
        assertFalse(state.expired());
        assertTrue(state.duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Remove" },
                declarations)).execute(player.state);

        assertFalse(state.applied());
        assertTrue(state.expired());
    }

    @Test
    public void testSetStateIndefinitelyAndRemember() throws Exception {
        Player player = TestUtils.createPlayer(StateCommandTest.class, "../StateCommandTest");
        State chastityDeviceState = player.state(Toys.Chastity_Device);
        State somethingOnPenisState = player.state(Body.SomethingOnPenis);

        Declarations declarations = new Declarations();
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STRING);

        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                Toys.class.getName(), Toys.Chastity_Device.name() + ".state.duration");

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Apply",
                "teaselib.Body.SomethingOnPenis", "over", "inf", "remember" }, declarations)).execute(player.state);

        assertTrue(stateStorage.available());
        assertTrue(chastityDeviceState.applied());
        assertFalse(chastityDeviceState.expired());
        assertTrue(chastityDeviceState.duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        assertTrue(somethingOnPenisState.applied());
        assertFalse(somethingOnPenisState.expired());

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Remove" },
                declarations)).execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(chastityDeviceState.applied());
        assertTrue(chastityDeviceState.expired());

        assertFalse(somethingOnPenisState.applied());
        assertTrue(somethingOnPenisState.expired());
    }
}
