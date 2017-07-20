package pcm.state.commands;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
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
    final Declarations declarations = new Declarations();

    Player player;

    public StateCommandTest() {
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STRING);
    }

    @Before
    public void initPlayer() throws Exception {
        player = TestUtils.createPlayer(StateCommandTest.class, "../StateCommandTest");
    }

    @Test
    public void testStateAppy() throws Exception {
        State state = player.state(Toys.Chastity_Device);

        StateCommand foo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "teaselib.Body.OnPenis" }, declarations));
        foo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());
        assertTrue(state.is(player.namespaceApplyAttribute));
        assertTrue(state.is(player.script.scriptApplyAttribute));

        StateCommand rememberFoo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "teaselib.Body.OnPenis", "Remember" },
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

        StateCommand fooTimed = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "teaselib.Body.OnPenis", "over", "10:00\"00" },
                declarations));
        fooTimed.execute(player.state);

        assertTrue(state.applied());
        assertFalse(state.expired());
        assertEquals(10, state.duration().limit(TimeUnit.HOURS));
    }

    @Test
    public void testStateApplyIndefinitely() throws Exception {
        State state = player.state(Toys.Chastity_Device);

        new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "teaselib.Body.OnPenis", "over", "inf" },
                declarations)).execute(player.state);

        assertTrue(state.applied());
        assertFalse(state.expired());
        assertTrue(state.duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Remove" },
                declarations)).execute(player.state);

        assertFalse(state.applied());
        assertTrue(state.expired());
    }

    @Test
    public void testStateApplyIndefinitelyAndRemember() throws Exception {
        testStateApplyToIndefinitelyAndRemember(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Apply",
                        "teaselib.Body.OnPenis", "over", "inf", "remember" }, declarations)).execute(player.state);
                return null;
            }
        });
    }

    @Test
    public void testStateApplyToIndefinitelyAndRemember() throws Exception {
        testStateApplyToIndefinitelyAndRemember(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Apply", "to",
                        "teaselib.Body.OnPenis", "over", "inf", "remember" }, declarations)).execute(player.state);
                return null;
            }
        });
    }

    public void testStateApplyToIndefinitelyAndRemember(Callable<Void> apply) throws Exception {
        State chastityDeviceState = player.state(Toys.Chastity_Device);
        State somethingOnPenisState = player.state(Body.OnPenis);

        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                Toys.class.getName(), Toys.Chastity_Device.name() + ".state.duration");

        apply.call();

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
