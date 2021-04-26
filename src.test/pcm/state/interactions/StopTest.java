package pcm.state.interactions;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import pcm.controller.LambdaTrigger;
import pcm.model.Action;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestPlayer;
import teaselib.core.Debugger.Response;

/**
 * @author Citizen-Cane
 *
 */
public class StopTest {

    final TestPlayer player;

    public StopTest() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        player = TestPlayer.loadScript(getClass());
    }

    @Test
    public void testIgnore() throws ScriptExecutionException {
        Action action = player.script.actions.get(1000);
        assertNotNull(action);
        player.debugger.addResponse("Stop!", Response.Ignore);
        player.play(action.number);
        assertEquals(ScriptState.SET, player.state.get(1001));
        assertEquals("Execution of action that contains the delay ... STOP statement", ScriptState.SET,
                player.state.get(8));
        assertEquals(ScriptState.SET, player.state.get(9992));
    }

    @Test
    public void testChoose() throws ScriptExecutionException {
        Action action = player.script.actions.get(1000);
        assertNotNull(action);
        player.debugger.addResponse("Stop!", Response.Choose);
        player.play(action.number);
        assertEquals(ScriptState.SET, player.state.get(1001));
        assertEquals("Execution of action that contains the delay ... STOP statement", ScriptState.SET,
                player.state.get(8));
        assertEquals(ScriptState.SET, player.state.get(9990));
    }

    @Test
    public void testLambdaTrigger() {
        Action stopAction = player.script.actions.get(1001);
        assertThrows(IllegalArgumentException.class,
                () -> new LambdaTrigger(stopAction, () -> player.debugger.replyScriptFunction("Stop!")));
    }

}
