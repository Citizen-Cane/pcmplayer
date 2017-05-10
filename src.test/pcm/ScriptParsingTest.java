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

public class ScriptParsingTest {

    @Test
    public void testDefineStatement()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript("ScriptParsingTest");

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        TestUtils.play(player, 1000);

        assertTrue(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        TestUtils.play(player, 1010);

        assertTrue(player.state(Toys.Collar).applied());
        assertFalse(player.state(Toys.Collar).expired());

        debugger.advanceTime(1, TimeUnit.HOURS);
        assertTrue(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());

        TestUtils.play(player, 1020);

        assertFalse(player.state(Toys.Collar).applied());
        assertTrue(player.state(Toys.Collar).expired());
    }

}
