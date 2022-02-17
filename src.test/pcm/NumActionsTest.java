package pcm;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import pcm.controller.AllActionsSetException;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestPlayer;

/**
 * @author Citizen-Cane
 *
 */
public class NumActionsTest {
    @Test
    public void testNumActionsFromOneShot()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());
        player.play(1000);
    }

    @Test
    public void testNumActionsFromRepeat()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertEquals(ScriptState.UNSET, player.state.get(1013));
        assertThatNumActionsFromWorks(player);
        assertEquals(ScriptState.SET, player.state.get(1013));

        assertThatNumActionsFromWorks(player);
        assertEquals(ScriptState.SET, player.state.get(1013));
    }

    private static void assertThatNumActionsFromWorks(TestPlayer player)
            throws AllActionsSetException, ScriptExecutionException {
        player.play(1010);
        assertEquals(ScriptState.UNSET, player.state.get(1013));

        for (int i = 0; i < 6; i++) {
            player.play(1011);
        }
        assertEquals(ScriptState.UNSET, player.state.get(1013));
        player.play(1011);
    }
}
