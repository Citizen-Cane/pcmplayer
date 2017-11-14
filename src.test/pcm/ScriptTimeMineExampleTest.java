/**
 * 
 */
package pcm;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.state.persistence.ScriptState;
import pcm.util.TestUtils;
import teaselib.core.Debugger;

/**
 * @author Citizen-Cane
 *
 */
public class ScriptTimeMineExampleTest {
    private final Player player;
    private final Debugger debugger;

    public ScriptTimeMineExampleTest() throws IOException {
        this.player = TestUtils.createPlayer(getClass());
        this.debugger = new Debugger(player.teaseLib);
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUpBefore() throws Exception {
        player.loadScript("ScriptTimeMineExampleTest");
    }

    private boolean containsAction(int n) {
        return player.range(new ActionRange(n)).contains(player.script.actions.get(n));
    }

    @Test
    public void testThatMineScriptExampleTablesAreCorrect() throws Exception {
        debugger.freezeTime();

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

        debugger.advanceTime(10, TimeUnit.MINUTES);

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

        debugger.advanceTime(10, TimeUnit.MINUTES);

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
