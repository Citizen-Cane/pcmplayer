package pcm;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestUtils;
import teaselib.core.Debugger;

/**
 * @author Citizen-Cane
 *
 */
public class NumActionsTest {
    @Test
    public void testNumActionsFromOneShot()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        TestUtils.play(player, 1000);
    }

    @Test
    public void testNumActionsFromRepeat()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        assertEquals(ScriptState.UNSET, player.state.get(1013));
        assertThatNumActionsFromWorks(player);
        assertEquals(ScriptState.SET, player.state.get(1013));

        assertThatNumActionsFromWorks(player);
        assertEquals(ScriptState.SET, player.state.get(1013));
    }

    private static void assertThatNumActionsFromWorks(Player player)
            throws AllActionsSetException, ScriptExecutionException {
        TestUtils.play(player, 1010);
        assertEquals(ScriptState.UNSET, player.state.get(1013));

        for (int i = 0; i < 6; i++) {
            TestUtils.play(player, 1011);
        }
        assertEquals(ScriptState.UNSET, player.state.get(1013));
        TestUtils.play(player, 1011);
    }
}
