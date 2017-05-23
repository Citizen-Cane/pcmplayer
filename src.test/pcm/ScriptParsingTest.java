package pcm;

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
import teaselib.Body;
import teaselib.Toys;
import teaselib.core.Debugger;

public class ScriptParsingTest {

    @Test
    public void testDefineStatement()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript("ScriptParsingTest_State");

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        TestUtils.play(player, 1000);

        assertTrue(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        TestUtils.play(player, 1001);

        assertTrue(player.state(Toys.Collar).applied());
        assertFalse(player.state(Toys.Collar).expired());

        debugger.advanceTime(1, TimeUnit.HOURS);
        assertTrue(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        TestUtils.play(player, 1002);

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());
    }

    @Test
    public void testCaseIndepencencyForState()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript("ScriptParsingTest_State");

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        TestUtils.play(player, 1010);

        assertTrue(player.state(Toys.Collar).applied());
        assertTrue(player.state(Body.AroundNeck).applied());
    }

    @Test
    public void testCaseIndepencencyForItems()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript("ScriptParsingTest_Items");

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        TestUtils.play(player, 1010);

        assertTrue(player.item(Toys.Collar).applied());
        assertTrue(player.state(Body.AroundNeck).applied());
    }

    @Test
    public void testCaseIndepencencyWithoutDeclarations()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript("ScriptParsingTest_WithoutDefinitions");

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        TestUtils.play(player, 1010);

        assertTrue(player.item(Toys.Collar).applied());
        assertTrue(player.item(Toys.Nipple_Clamps).applied());
        assertTrue(player.item(Toys.Buttplug).applied());
        assertTrue(player.state(Body.AroundNeck).applied());
    }
}
