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
import teaselib.util.Item;

public class ItemCommandTest {

    @Test
    public void testSetItem() throws Exception {
        Player player = TestUtils.createPlayer(StateCommandTest.class, "../ItemCommandTest");
        Item chastityDeviceItem = player.item(Toys.Chastity_Device);
        State somethingOnPenisState = player.state(Body.OnPenis);

        Declarations declarations = new Declarations();
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STRING);

        ItemCommand foo = new ItemCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply" }, declarations));
        foo.execute(player.state);

        assertTrue(chastityDeviceItem.applied());
        assertTrue(chastityDeviceItem.expired());
        assertTrue(chastityDeviceItem.is(player.namespaceApplyAttribute));
        assertTrue(chastityDeviceItem.is(player.script.scriptApplyAttribute));

        assertTrue(somethingOnPenisState.applied());
        assertTrue(somethingOnPenisState.expired());

        ItemCommand rememberFoo = new ItemCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "Remember" }, declarations));
        rememberFoo.execute(player.state);

        assertTrue(chastityDeviceItem.applied());
        assertTrue(chastityDeviceItem.expired());

        assertTrue(somethingOnPenisState.applied());
        assertTrue(somethingOnPenisState.expired());

        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                Toys.class.getName(), Toys.Chastity_Device.name() + ".state.duration");

        assertTrue(stateStorage.available());

        ItemCommand clearFoo = new ItemCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Remove" }, declarations));
        clearFoo.execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(chastityDeviceItem.applied());
        assertTrue(chastityDeviceItem.expired());

        assertFalse(somethingOnPenisState.applied());
        assertTrue(somethingOnPenisState.expired());

        ItemCommand fooTimed = new ItemCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "over", "10:00\"00" }, declarations));
        fooTimed.execute(player.state);

        assertTrue(chastityDeviceItem.applied());
        assertFalse(chastityDeviceItem.expired());
        assertEquals(10, player.state(Toys.Chastity_Device).duration().limit(TimeUnit.HOURS));

        assertTrue(somethingOnPenisState.applied());
        assertFalse(somethingOnPenisState.expired());
    }

    @Test
    public void testSetItemIndefinitely() throws Exception {
        Player player = TestUtils.createPlayer(StateCommandTest.class, "../ItemCommandTest");
        Item chastityDeviceItem = player.item(Toys.Chastity_Device);
        State somethingOnPenisState = player.state(Body.OnPenis);
        Declarations declarations = new Declarations();
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STRING);

        new ItemCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply", "over", "inf" }, declarations))
                        .execute(player.state);

        assertTrue(chastityDeviceItem.applied());
        assertFalse(chastityDeviceItem.expired());
        assertTrue(player.state(Toys.Chastity_Device).duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        assertTrue(somethingOnPenisState.applied());
        assertFalse(somethingOnPenisState.expired());

        new ItemCommand(new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Remove" },
                declarations)).execute(player.state);

        assertFalse(chastityDeviceItem.applied());
        assertTrue(chastityDeviceItem.expired());

        assertFalse(somethingOnPenisState.applied());
        assertTrue(somethingOnPenisState.expired());
    }

    @Test
    public void testSetItemIndefinitelyAndRemember() throws Exception {
        Player player = TestUtils.createPlayer(StateCommandTest.class, "../ItemCommandTest");
        Item chastityDeviceItem = player.item(Toys.Chastity_Device);
        State somethingOnPenisState = player.state(Body.OnPenis);

        Declarations declarations = new Declarations();
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STRING);

        TeaseLib.PersistentString stateStorage = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                Toys.class.getName(), Toys.Chastity_Device.name() + ".state.duration");

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Apply",
                "teaselib.Body.OnPenis", "over", "inf", "remember" }, declarations)).execute(player.state);

        assertTrue(stateStorage.available());
        assertTrue(chastityDeviceItem.applied());
        assertFalse(chastityDeviceItem.expired());
        assertTrue(player.state(Toys.Chastity_Device).duration().limit(TimeUnit.SECONDS) == Long.MAX_VALUE);

        assertTrue(somethingOnPenisState.applied());
        assertFalse(somethingOnPenisState.expired());

        new StateCommand(new StateCommandLineParameters(new String[] { "teaselib.Toys.Chastity_Device", "Remove" },
                declarations)).execute(player.state);

        assertFalse(stateStorage.available());
        assertFalse(chastityDeviceItem.applied());
        assertTrue(chastityDeviceItem.expired());

        assertFalse(somethingOnPenisState.applied());
        assertTrue(somethingOnPenisState.expired());

    }
}
