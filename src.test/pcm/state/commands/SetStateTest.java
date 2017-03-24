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
                new String[] { "Apply", "teaselib.Body.SomethingOnPenis",
                        "teaselib.Toys.Chastity_Cage", "Remember" });
        rememberFoo.execute(player.state);

        assertTrue(state.applied());
        assertTrue(state.expired());

        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(
                TeaseLib.DefaultDomain, Body.class.getName(),
                Body.SomethingOnPenis.name() + ".state.duration");

        assertTrue(stateStorage.available());

        SetState clearFoo = new SetState(
                new String[] { "Remove", "teaselib.Body.SomethingOnPenis" });
        clearFoo.execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(state.applied());
        assertTrue(state.expired());

        SetState fooTimed = new SetState(
                new String[] { "Apply", "teaselib.Body.SomethingOnPenis",
                        "teaselib.Toys.Chastity_Cage", "600" });
        fooTimed.execute(player.state);

        assertTrue(state.applied());
        assertFalse(state.expired());
        assertEquals(600, state.duration().limit(TimeUnit.MINUTES));
    }

    @Test
    public void testSetStateIndefinitely() throws Exception {
        Player player = TestUtils.createPlayer(SetStateTest.class);
        State state = player.state(Body.SomethingOnPenis);

        new SetState(new String[] { "Apply", "teaselib.Body.SomethingOnPenis",
                "teaselib.Toys.Chastity_Cage", "inf" }).execute(player.state);

        assertTrue(state.applied());
        assertFalse(state.expired());
        assertTrue(state.duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        new SetState(
                new String[] { "Remove", "teaselib.Body.SomethingOnPenis" })
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

        new SetState(new String[] { "Apply", "teaselib.Body.SomethingOnPenis",
                "teaselib.Toys.Chastity_Cage", "inf", "remember" })
                        .execute(player.state);

        assertTrue(stateStorage.available());
        assertTrue(state.applied());
        assertFalse(state.expired());
        assertTrue(state.duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        new SetState(
                new String[] { "Remove", "teaselib.Body.SomethingOnPenis" })
                        .execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(state.applied());
        assertTrue(state.expired());

    }
}
