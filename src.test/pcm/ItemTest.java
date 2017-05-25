package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.util.TestUtils;
import teaselib.Toys;
import teaselib.core.Debugger;

public class ItemTest {

    @Test
    public void testApplyItem() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        assertFalse(player.item(Toys.Collar).applied());
        assertTrue(player.item(Toys.Collar).expired());

        TestUtils.play(player, 1000);

        assertTrue(player.item(Toys.Collar).applied());
        assertFalse(player.item(Toys.Collar).expired());

        debugger.advanceTime(1, TimeUnit.HOURS);

        TestUtils.play(player, 1008);

        assertFalse(player.item(Toys.Collar).applied());
        assertTrue(player.item(Toys.Collar).expired());
    }

    @Test
    public void testApplyItemTo()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        TestUtils.play(player, 1010);

    }

    @Test
    public void testRemainingDuration()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        player.state(Toys.Nipple_Clamps).apply().over(30, TimeUnit.MINUTES);
        TestUtils.play(player, 1040);

        debugger.advanceTime(30, TimeUnit.MINUTES);
        TestUtils.play(player, 1041);

        debugger.advanceTime(10, TimeUnit.MINUTES);
        TestUtils.play(player, 1042);
    }

    @Test
    public void testElapsedDuration()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        player.item(Toys.Nipple_Clamps).apply().over(30, TimeUnit.MINUTES);
        assertEquals(0, player.item(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        TestUtils.play(player, 1050);

        debugger.advanceTime(10, TimeUnit.MINUTES);
        assertEquals(10, player.item(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        TestUtils.play(player, 1051);

        debugger.advanceTime(20, TimeUnit.MINUTES);
        assertEquals(30, player.item(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        TestUtils.play(player, 1052);

        debugger.advanceTime(10, TimeUnit.MINUTES);
        assertEquals(40, player.item(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        TestUtils.play(player, 1053);
    }

    @Test
    public void testIfStateCondition()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        TestUtils.play(player, 1060);
        TestUtils.play(player, 1065);
        player.item(Toys.Nipple_Clamps).apply().over(30, TimeUnit.MINUTES);
        TestUtils.play(player, 1060);
        TestUtils.play(player, 1065);
    }

}
