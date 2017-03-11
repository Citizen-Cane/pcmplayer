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

        SetState foo = new SetState(
                new String[] { "Apply", "teaselib.Body.SomethingOnPenis",
                        "teaselib.Toys.Chastity_Cage" });
        foo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());

        SetState rememberFoo = new SetState(
                new String[] { "Remember", "teaselib.Body.SomethingOnPenis" });
        rememberFoo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());
        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(
                TeaseLib.DefaultDomain, Body.class.getName(),
                Body.SomethingOnPenis.name() + ".state");

        assertTrue(stateStorage.available());

        SetState clearFoo = new SetState(
                new String[] { "Clear", "teaselib.Body.SomethingOnPenis" });
        clearFoo.execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(state.applied());
        assertTrue(state.freeSince(0, TimeUnit.MINUTES));

        SetState fooTimed = new SetState(
                new String[] { "Apply", "teaselib.Body.SomethingOnPenis",
                        "teaselib.Toys.Chastity_Cage", "600" });
        fooTimed.execute(player.state);

        assertTrue(state.applied());
        assertEquals(600 * 60, state.expected());
    }
}
