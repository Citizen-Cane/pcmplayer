/**
 * 
 */
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
import pcm.state.persistence.ScriptState;
import pcm.util.TestPlayer;

/**
 * @author Citizen-Cane
 *
 */
public class ScriptTimeMineExampleTest {
    private final TestPlayer player;

    public ScriptTimeMineExampleTest()
            throws IOException, ScriptParsingException, ScriptExecutionException, ValidationIssue {
        this.player = TestPlayer.loadScript(getClass(), "ScriptTimeMineExampleTest");
    }

    private boolean containsAction(int n) {
        return player.range(new ActionRange(n)).contains(player.script.actions.get(n));
    }

    @Test
    public void testThatMineScriptExampleTablesAreCorrect() throws Exception {
        ActionRange r = new ActionRange(1000);
        player.playFrom(r);
        assertEquals(ScriptState.SET, player.state.get(1000));

        assertFalse(containsAction(1001));
        assertTrue(containsAction(1002));
        assertTrue(containsAction(1003));
        assertTrue(containsAction(1004));
        assertTrue(containsAction(1005));
        assertTrue(containsAction(1006));
        assertFalse(containsAction(1007));
        assertFalse(containsAction(1008));
        assertFalse(containsAction(1009));
        assertFalse(containsAction(1010));

        player.debugger.advanceTime(10, TimeUnit.MINUTES);

        assertFalse(containsAction(1001));
        assertFalse(containsAction(1002));
        assertTrue(containsAction(1003));
        assertTrue(containsAction(1004));
        assertTrue(containsAction(1005));
        assertTrue(containsAction(1006));
        assertTrue(containsAction(1007));
        assertTrue(containsAction(1008));
        assertFalse(containsAction(1009));
        assertFalse(containsAction(1010));

        player.debugger.advanceTime(10, TimeUnit.MINUTES);

        assertFalse(containsAction(1001));
        assertFalse(containsAction(1002));
        assertFalse(containsAction(1003));
        assertFalse(containsAction(1004));
        assertTrue(containsAction(1005));
        assertTrue(containsAction(1006));
        assertTrue(containsAction(1007));
        assertTrue(containsAction(1008));
        assertTrue(containsAction(1009));
        assertTrue(containsAction(1010));
    }
}
