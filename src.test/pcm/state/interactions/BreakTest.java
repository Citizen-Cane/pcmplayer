package pcm.state.interactions;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import pcm.controller.LambdaTrigger;
import pcm.model.Action;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestPlayer;
import teaselib.core.Debugger.Response;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Citizen-Cane
 *
 */
public class BreakTest {

    final TestPlayer player;

    public BreakTest() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        player = TestPlayer.loadScript(getClass());
    }

    @Test
    public void testIgnore() throws ScriptExecutionException {
        Action action = player.script.actions.get(1000);
        assertNotNull(action);
        player.debugger.addResponse("Stop!", Response.Ignore);
        player.play(action.number);
        assertEquals(ScriptState.SET, player.state.get(1001));
        assertEquals(ScriptState.SET, player.state.get(8));
        assertEquals(ScriptState.SET, player.state.get(9992));
    }

    @Test
    public void testChoose() throws ScriptExecutionException {
        Action action = player.script.actions.get(1000);
        assertNotNull(action);
        player.debugger.addResponse("Stop!", Response.Choose);
        player.play(action.number);
        assertEquals(ScriptState.SET, player.state.get(1001));
        assertEquals(ScriptState.UNSET, player.state.get(8),
                "Break into stop range before executing action");
        assertEquals(ScriptState.SET, player.state.get(9990), "Stop range");
    }

    @Test
    public void testLambdaTrigger() throws ScriptExecutionException {
        Action action = player.script.actions.get(1000);
        assertNotNull(action);

        player.breakPoints.add(player.script.name, new LambdaTrigger(
                player.script.actions.get(1002),
                () -> player.debugger.replyScriptFunction("Stop!")));
        player.play(action.number);

        assertEquals(ScriptState.SET, player.state.get(1001));
        assertEquals(ScriptState.UNSET, player.state.get(8),
                "Break into stop range before executing action");
        assertEquals(ScriptState.SET, player.state.get(9990), "Stop range");
    }

    @Test
    public void testBreakStatementNesting() throws ScriptExecutionException {
        Action action = player.script.actions.get(1010);
        player.debugger.addResponse("Stop!", Response.Ignore);
        assertNotNull(action);
        try {
            player.play(action.number);
            fail("nested break statement not detected");
        } catch (ScriptExecutionException e) {
            if (e.action != action) {
                throw e;
            }
        }

    }

}
