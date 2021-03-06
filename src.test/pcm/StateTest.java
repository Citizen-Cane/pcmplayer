package pcm;

import static org.junit.Assert.*;

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

        player.state(Toys.Nipple_Clamps).applyTo().over(30, TimeUnit.MINUTES);
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

        long start = player.teaseLib.getTime(TimeUnit.MILLISECONDS);

        player.state(Toys.Nipple_Clamps).applyTo().over(30, TimeUnit.MINUTES);
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

        long end = player.teaseLib.getTime(TimeUnit.MILLISECONDS);
        assertEquals(40 * 60 * 1000, end - start);
    }

    @Test
    public void testIfStateConditionOr()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        TestUtils.play(player, 1060);
        TestUtils.play(player, 1065);
        player.state(Toys.Nipple_Clamps).applyTo().over(30, TimeUnit.MINUTES);
        TestUtils.play(player, 1060);
        TestUtils.play(player, 1065);
    }

    @Test
    public void testIfStateConditionAnd()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        player.state(Toys.Nipple_Clamps).applyTo();
        TestUtils.play(player, 1070);
        player.state(Toys.Nipple_Clamps).remove();
        TestUtils.play(player, 1076);

        player.state(Toys.Nipple_Clamps).applyTo().over(30, TimeUnit.MINUTES);
        TestUtils.play(player, 1071);
        player.state(Toys.Nipple_Clamps).remove();
        TestUtils.play(player, 1075);
        debugger.advanceTime(30, TimeUnit.MINUTES);
        TestUtils.play(player, 1076);
    }

    @Test
    public void testAppliedTo() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        TestUtils.play(player, 1090);
    }

    @Test
    public void testIfStateConditionOrMultipleItems()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        player.state(Toys.Nipple_Clamps).remove();
        player.state(Toys.Gag).apply();
        TestUtils.play(player, 1101);
        TestUtils.play(player, 1102);

        player.state(Toys.Nipple_Clamps).apply();
        player.state(Toys.Gag).remove();
        TestUtils.play(player, 1101);
        TestUtils.play(player, 1102);

        player.state(Toys.Nipple_Clamps).apply();
        player.state(Toys.Gag).apply();
        TestUtils.play(player, 1100);
        TestUtils.play(player, 1101);
        TestUtils.play(player, 1102);

        player.state(Toys.Nipple_Clamps).remove();
        player.state(Toys.Gag).remove();
        TestUtils.play(player, 1103);

    }

    // TODO Remove multiple peers from object
    // (Household.Clothes_Pegs used in Mine)

    // TODO Test (again?) that removing single items is symmetric (at least for injected peers)
}
