package pcm.state.commands;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Declarations;
import pcm.controller.Player;
import pcm.state.StateCommandLineParameters;
import pcm.util.TestUtils;
import teaselib.State;
import teaselib.Toys;

public class StateCommandTest {
    final Declarations declarations = new Declarations();

    Player player;

    public StateCommandTest() {
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STRING);
    }

    @Before
    public void initPlayer() throws Exception {
        player = TestUtils.createPlayer(StateCommandTest.class, "StateCommandTest");
    }

    @Test
    public void testStateApply() throws Exception {
        State state = player.state(Toys.Chastity_Device);

        StateCommand foo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "teaselib.Body.OnPenis" }, declarations));
        foo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());
        assertTrue(state.is(player.namespaceApplyAttribute));
        assertTrue(state.is(player.script.scriptApplyAttribute));

        StateCommand clearFoo = new StateCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Remove" }, declarations));
        clearFoo.execute(player.state);

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
        assertFalse(state.expired());
    }
}
