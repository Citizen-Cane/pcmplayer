package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.util.TestPlayer;
import teaselib.Household;
import teaselib.Toys;

public class ItemTest {

    @Test
    public void testApplyItem() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertFalse(player.item(Toys.Collar).applied());
        assertTrue(player.item(Toys.Collar).expired());

        player.play(1000);

        assertTrue(player.item(Toys.Collar).applied());
        assertFalse(player.item(Toys.Collar).expired());

        player.debugger.advanceTime(1, TimeUnit.HOURS);

        player.play(1008);

        assertFalse(player.item(Toys.Collar).applied());
        assertTrue(player.item(Toys.Collar).expired());
    }

    @Test
    public void testApplyItemTo()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());
        player.play(1010);
    }

    @Test
    public void testStateNamespaceAndApplyTagsInScript()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertFalse(player.item(Toys.Nipple_Clamps).applied());
        assertTrue(player.item(Toys.Nipple_Clamps).expired());

        player.play(1030);

        assertFalse(player.item(Toys.Nipple_Clamps).is(TestPlayer.NAMESPACE));

        assertFalse(player.item(Toys.Nipple_Clamps).applied());
        assertTrue(player.item(Toys.Nipple_Clamps).expired());

        assertFalse(player.item(Toys.Nipple_Clamps).is("Applied.by.StateTest"));
        assertFalse(player.item(Toys.Nipple_Clamps).is("Applied.by.StateTest_subscript"));
    }

    @Test
    public void testRemainingDuration()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        player.state(Toys.Nipple_Clamps).applyTo().over(30, TimeUnit.MINUTES);
        assertEquals(1800, player.state(Toys.Nipple_Clamps).duration().remaining(TimeUnit.SECONDS));
        player.play(1040);

        player.debugger.advanceTime(30, TimeUnit.MINUTES);
        assertEquals(0, player.state(Toys.Nipple_Clamps).duration().remaining(TimeUnit.SECONDS));
        player.play(1041);

        player.debugger.advanceTime(10, TimeUnit.MINUTES);
        assertEquals(-600, player.state(Toys.Nipple_Clamps).duration().remaining(TimeUnit.SECONDS));
        player.play(1042);
    }

    @Test
    public void testElapsedDuration()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        player.item(Toys.Nipple_Clamps).apply().over(30, TimeUnit.MINUTES);
        assertEquals(0, player.item(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        player.play(1050);

        player.debugger.advanceTime(10, TimeUnit.MINUTES);
        assertEquals(10, player.item(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        player.play(1051);

        player.debugger.advanceTime(20, TimeUnit.MINUTES);
        assertEquals(30, player.item(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        player.play(1052);

        player.debugger.advanceTime(10, TimeUnit.MINUTES);
        assertEquals(40, player.item(Toys.Nipple_Clamps).duration().elapsed(TimeUnit.MINUTES));
        player.play(1053);
    }

    @Test
    public void testIfItemConditionOr()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        player.play(1060);
        player.play(1065);
        player.item(Toys.Nipple_Clamps).apply().over(30, TimeUnit.MINUTES);
        player.play(1060);
        player.play(1065);
    }

    @Test
    public void testIfStateConditionAnd()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        player.item(Toys.Nipple_Clamps).apply();
        player.play(1070);
        player.item(Toys.Nipple_Clamps).remove();
        player.play(1076);

        player.item(Toys.Nipple_Clamps).apply().over(30, TimeUnit.MINUTES);
        player.play(1071);
        player.item(Toys.Nipple_Clamps).remove();
        player.play(1075);
        player.debugger.advanceTime(30, TimeUnit.MINUTES);
        player.play(1076);
    }

    @Test
    public void testCanApplyTo() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());
        player.play(1080);
    }

    @Test
    public void testAppliedTo() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());
        player.play(1090);
    }

    @Test
    public void testIfItemConditionOrMultipleItems()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertFalse(player.item(Toys.Nipple_Clamps).applied());
        player.item(Toys.Gag).apply();
        player.play(1101);
        player.play(1102);

        player.item(Toys.Nipple_Clamps).apply();
        player.item(Toys.Gag).remove();
        player.play(1101);
        player.play(1102);

        assertTrue(player.item(Toys.Nipple_Clamps).applied());
        player.item(Toys.Gag).apply();
        player.play(1100);
        player.play(1101);
        player.play(1102);

        player.item(Toys.Nipple_Clamps).remove();
        player.item(Toys.Gag).remove();
        player.play(1103);

    }

    @Test
    public void testApplyAndRemoveOneAfterAnother()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());
        player.play(1120);

    }

    @Test
    public void testItemMatching()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());
        player.play(1110);
    }

    @Test
    public void testStateNamespaceAndApplyToTagsInScript()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertFalse(player.item(Household.Clothes_Pegs).applied());
        assertTrue(player.item(Household.Clothes_Pegs).expired());

        player.play(1130);

        assertFalse(player.item(Household.Clothes_Pegs).is(TestPlayer.NAMESPACE));

        assertFalse(player.item(Household.Clothes_Pegs).applied());
        assertTrue(player.item(Household.Clothes_Pegs).expired());

        assertFalse(player.item(Household.Clothes_Pegs).is("Applied.by.StateTest"));
        assertFalse(player.item(Household.Clothes_Pegs).is("Applied.by.StateTest_subscript"));
    }

}
