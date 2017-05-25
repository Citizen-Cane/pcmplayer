package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.util.TestUtils;
import teaselib.Toys;
import teaselib.core.Debugger;

public class StateTest {

    @Test
    public void testState() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        TestUtils.play(player, 1000);

        assertTrue(player.state(Toys.Collar).applied());
        assertFalse(player.state(Toys.Collar).expired());

        assertTrue(player.state(Toys.Collar).is(TestUtils.TEST_NAMESPACE));
        assertTrue(player.state(Toys.Collar).is("Applied.by.StateTest"));
        assertFalse(player.state(Toys.Collar).is("Applied.by.StateTest_subscript"));

        debugger.advanceTime(1, TimeUnit.HOURS);
        TestUtils.play(player, 1008);

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        assertFalse(player.state(Toys.Collar).is(TestUtils.TEST_NAMESPACE));
        assertFalse(player.state(Toys.Collar).is("Applied.by.StateTest"));
        assertFalse(player.state(Toys.Collar).is("Applied.by.StateTest_subscript"));
    }

    @Test
    public void testStateNamespaceAndApplyTags()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        assertFalse(player.state(Toys.Nipple_Clamps).applied());
        assertTrue(player.state(Toys.Nipple_Clamps).expired());

        TestUtils.play(player, 1020);

        assertTrue(player.state(Toys.Nipple_Clamps).is(TestUtils.TEST_NAMESPACE));

        assertFalse(player.state(Toys.Nipple_Clamps).is("Applied.by.StateTest"));
        assertTrue(player.state(Toys.Nipple_Clamps).is("Applied.by.StateTest_subscript"));
    }

    @Test
    public void testStateNamespaceAndApplyTagsInScript()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        assertFalse(player.state(Toys.Nipple_Clamps).applied());
        assertTrue(player.state(Toys.Nipple_Clamps).expired());

        TestUtils.play(player, 1030);

        assertFalse(player.state(Toys.Nipple_Clamps).is(TestUtils.TEST_NAMESPACE));

        assertFalse(player.state(Toys.Nipple_Clamps).applied());
        assertTrue(player.state(Toys.Nipple_Clamps).expired());

        assertFalse(player.state(Toys.Nipple_Clamps).is("Applied.by.StateTest"));
        assertFalse(player.state(Toys.Nipple_Clamps).is("Applied.by.StateTest_subscript"));
    }

    @Test
    public void testStateCorrectlyRemoved()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        TestUtils.play(player, new ActionRange(1000), new ActionRange(1000, 1001));

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());
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

        player.state(Toys.Nipple_Clamps).apply().over(30, TimeUnit.MINUTES);
        assertEquals(0, player.state(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        TestUtils.play(player, 1050);

        debugger.advanceTime(10, TimeUnit.MINUTES);
        assertEquals(10, player.state(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        TestUtils.play(player, 1051);

        debugger.advanceTime(20, TimeUnit.MINUTES);
        assertEquals(30, player.state(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        TestUtils.play(player, 1052);

        debugger.advanceTime(10, TimeUnit.MINUTES);
        assertEquals(40, player.state(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        TestUtils.play(player, 1053);
    }
}
