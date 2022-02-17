package pcm.state.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Declarations;
import pcm.controller.Player;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.StateCommandLineParameters;
import pcm.util.TestPlayer;
import teaselib.Body;
import teaselib.State;
import teaselib.Toys;
import teaselib.util.Item;

public class ItemCommandTest {
    final Declarations declarations = new Declarations();

    Player player;

    @Before
    public void initPlayer() throws ScriptParsingException, ScriptExecutionException, ValidationIssue, IOException {
        player = TestPlayer.loadScript(StateCommandTest.class, "ItemCommandTest");
    }

    public ItemCommandTest() {
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Toys", Declarations.ITEM);
        declarations.add("teaselib.Body", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STATE);
    }

    @Test
    public void testItemApply() throws Exception {
        Item chastityDeviceItem = player.item(Toys.Chastity_Device);
        State somethingOnPenisState = player.state(Body.OnPenis);

        ItemCommand foo = new ItemCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Apply" }, declarations));
        foo.execute(player.state);

        assertTrue(chastityDeviceItem.applied());
        assertTrue(chastityDeviceItem.expired());
        assertTrue(chastityDeviceItem.is(player.namespaceApplyAttribute));
        assertTrue(chastityDeviceItem.is(player.script.scriptApplyAttribute));

        assertTrue(somethingOnPenisState.applied());
        assertTrue(somethingOnPenisState.expired());

        ItemCommand clearFoo = new ItemCommand(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "Remove" }, declarations));
        clearFoo.execute(player.state);

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
    public void testItemApplyIndefinitely() throws Exception {
        Item chastityDeviceItem = player.item(Toys.Chastity_Device);
        State somethingOnPenisState = player.state(Body.OnPenis);

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
        assertFalse(chastityDeviceItem.expired()); // Item never expires

        assertFalse(somethingOnPenisState.applied());
        assertTrue(somethingOnPenisState.expired());
    }
}
