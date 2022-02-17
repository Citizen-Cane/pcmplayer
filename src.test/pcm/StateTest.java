package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.util.TestPlayer;
import teaselib.Toys;

public class StateTest {

    @Test
    public void testState() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        player.play(1000);

        assertTrue(player.state(Toys.Collar).applied());
        assertFalse(player.state(Toys.Collar).expired());

        assertTrue(player.state(Toys.Collar).is(TestPlayer.NAMESPACE));
        assertTrue(player.state(Toys.Collar).is("Applied.by.StateTest"));
        assertFalse(player.state(Toys.Collar).is("Applied.by.StateTest_subscript"));

        player.debugger.advanceTime(1, TimeUnit.HOURS);
        player.play(1008);

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        assertFalse(player.state(Toys.Collar).is(TestPlayer.NAMESPACE));
        assertFalse(player.state(Toys.Collar).is("Applied.by.StateTest"));
        assertFalse(player.state(Toys.Collar).is("Applied.by.StateTest_subscript"));
    }

    @Test
    public void testStateNamespaceAndApplyTags()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertFalse(player.state(Toys.Nipple_Clamps).applied());
        assertTrue(player.state(Toys.Nipple_Clamps).expired());

        player.play(1020);

        assertTrue(player.state(Toys.Nipple_Clamps).is(TestPlayer.NAMESPACE));

        assertFalse(player.state(Toys.Nipple_Clamps).is("Applied.by.StateTest"));
        assertTrue(player.state(Toys.Nipple_Clamps).is("Applied.by.StateTest_subscript"));
    }

    @Test
    public void testStateNamespaceAndApplyTagsInScript()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertFalse(player.state(Toys.Nipple_Clamps).applied());
        assertTrue(player.state(Toys.Nipple_Clamps).expired());

        player.play(1030);

        assertFalse(player.state(Toys.Nipple_Clamps).is(TestPlayer.NAMESPACE));

        assertFalse(player.state(Toys.Nipple_Clamps).applied());
        assertTrue(player.state(Toys.Nipple_Clamps).expired());

        assertFalse(player.state(Toys.Nipple_Clamps).is("Applied.by.StateTest"));
        assertFalse(player.state(Toys.Nipple_Clamps).is("Applied.by.StateTest_subscript"));
    }

    @Test
    public void testStateCorrectlyRemoved()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        player.play(new ActionRange(1000), new ActionRange(1000, 1001));

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());
    }

    @Test
    public void testRemainingDuration()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        player.state(Toys.Nipple_Clamps).applyTo().over(30, TimeUnit.MINUTES);
        player.play(1040);

        player.debugger.advanceTime(30, TimeUnit.MINUTES);
        player.play(1041);

        player.debugger.advanceTime(10, TimeUnit.MINUTES);
        player.play(1042);
    }

    @Test
    public void testElapsedDuration()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        long start = player.teaseLib.getTime(TimeUnit.MILLISECONDS);

        player.state(Toys.Nipple_Clamps).applyTo().over(30, TimeUnit.MINUTES);
        assertEquals(0, player.state(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));

        player.play(1050);

        player.debugger.advanceTime(10, TimeUnit.MINUTES);
        assertEquals(10, player.state(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        player.play(1051);

        player.debugger.advanceTime(20, TimeUnit.MINUTES);
        assertEquals(30, player.state(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        player.play(1052);

        player.debugger.advanceTime(10, TimeUnit.MINUTES);
        assertEquals(40, player.state(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        player.play(1053);

        long end = player.teaseLib.getTime(TimeUnit.MILLISECONDS);
        assertEquals(40 * 60 * 1000, end - start);
    }

    @Test
    public void testIfStateConditionOr()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        player.play(1060);
        player.play(1065);
        player.state(Toys.Nipple_Clamps).applyTo().over(30, TimeUnit.MINUTES);
        player.play(1060);
        player.play(1065);
    }

    @Test
    public void testIfStateConditionAnd()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        player.state(Toys.Nipple_Clamps).applyTo();
        player.play(1070);
        player.state(Toys.Nipple_Clamps).remove();
        player.play(1076);

        player.state(Toys.Nipple_Clamps).applyTo().over(30, TimeUnit.MINUTES);
        player.play(1071);
        player.state(Toys.Nipple_Clamps).remove();
        player.play(1075);
        player.debugger.advanceTime(30, TimeUnit.MINUTES);
        player.play(1076);
    }

    @Test
    public void testAppliedTo() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());
        player.play(1090);
    }

    @Test
    public void testIfStateConditionOrMultipleItems()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        player.state(Toys.Nipple_Clamps).remove();
        player.state(Toys.Gag).apply();
        player.play(1101);
        player.play(1102);

        player.state(Toys.Nipple_Clamps).apply();
        player.state(Toys.Gag).remove();
        player.play(1101);
        player.play(1102);

        player.state(Toys.Nipple_Clamps).apply();
        player.state(Toys.Gag).apply();
        player.play(1100);
        player.play(1101);
        player.play(1102);

        player.state(Toys.Nipple_Clamps).remove();
        player.state(Toys.Gag).remove();
        player.play(1103);

    }

    // TODO Remove multiple peers from object
    // (Household.Clothes_Pegs used in Mine)

    // TODO Test (again?) that removing single items is symmetric (at least for injected peers)
}
