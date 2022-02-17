package pcm;

import java.io.IOException;

import org.junit.Test;

import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.util.TestPlayer;
import teaselib.core.Debugger;

public class GosubTest {

    @Test
    public void testGosubIsExecutedAfterActionCommands()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());
        player.debugger.addResponse("Interaction", () -> {
            player.state.set(3);
            return Debugger.Response.Choose;
        });

        player.play(new ActionRange(1000));
    }

    @Test
    public void testMultipleGosubsAreExecutedOneAfterAnotherPromptFirst()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());
        player.debugger.addResponse("Interaction", () -> {
            player.state.set(5);
            return Debugger.Response.Choose;
        });

        player.play(new ActionRange(1010));
    }

    @Test
    public void testMultipleGosubsAreExecutedOneAfterAnotherPromptLast()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());
        player.debugger.addResponse("Interaction", () -> {
            player.state.set(5);
            return Debugger.Response.Choose;
        });

        player.play(new ActionRange(1020));
    }
}
