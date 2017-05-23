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
}
