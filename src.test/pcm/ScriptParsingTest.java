package pcm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.util.TestPlayer;
import teaselib.Body;
import teaselib.Toys;

public class ScriptParsingTest {

    @Test
    public void testDefineStatement()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass(), "ScriptParsingTest_State");

        assertFalse(player.state(Body.AroundNeck).applied());
        assertTrue(player.state(Body.AroundNeck).expired());

        player.play(1000);

        assertTrue(player.state(Body.AroundNeck).applied());
        assertTrue(player.state(Body.AroundNeck).expired());

        player.play(1001);

        assertTrue(player.state(Body.AroundNeck).applied());
        assertFalse(player.state(Body.AroundNeck).expired());

        player.debugger.advanceTime(1, TimeUnit.HOURS);
        assertTrue(player.state(Body.AroundNeck).applied());
        assertTrue(player.state(Body.AroundNeck).expired());

        player.play(1002);

        assertFalse(player.state(Body.AroundNeck).applied());
        assertTrue(player.state(Body.AroundNeck).expired());
    }

    @Test
    public void testCaseIndepencencyForState()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass(), "ScriptParsingTest_State");

        player.play(1010);
        assertTrue(player.state(Toys.Collar).applied());
        assertTrue(player.state(Body.AroundNeck).applied());
    }

    @Test
    public void testCaseIndepencencyForItems()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass(), "ScriptParsingTest_Items");

        player.play(1010);
        assertTrue(player.item(Toys.Collar).applied());
        assertTrue(player.state(Body.AroundNeck).applied());
    }

    @Test
    public void testCaseIndepencencyWithoutDeclarations()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass(), "ScriptParsingTest_WithoutDefinitions");

        player.play(1010);
        assertTrue(player.item(Toys.Collar).applied());
        assertTrue(player.item(Toys.Nipple_Clamps).applied());
        assertTrue(player.item(Toys.Buttplug).applied());
        assertTrue(player.state(Body.AroundNeck).applied());
    }
}
