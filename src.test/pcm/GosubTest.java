package pcm;

import java.io.IOException;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.util.TestUtils;
import teaselib.core.Debugger;

public class GosubTest {
    enum Body {
        Chastified
    }

    @Test
    public void testGosubIsExecutedAfterActionCommands()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.addResponse("Interaction", () -> {
            player.state.set(3);
            return Debugger.Response.Choose;
        });

        pcm.util.TestUtils.play(player, new ActionRange(1000), null);
    }

    @Test
    public void testMultipleGosubsAreExecutedOneAfterAnotherPromptFirst()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.addResponse("Interaction", () -> {
            player.state.set(5);
            return Debugger.Response.Choose;
        });

        pcm.util.TestUtils.play(player, new ActionRange(1010), null);
    }

    @Test(expected = ScriptParsingException.class)
    public void testMultipleGosubsAreExecutedOneAfterAnotherPromptLast()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName() + "2");

        Debugger debugger = new Debugger(player.teaseLib);
        debugger.addResponse("Interaction", () -> {
            player.state.set(5);
            return Debugger.Response.Choose;
        });

        pcm.util.TestUtils.play(player, new ActionRange(1020), null);
        // TODO fails because reply() is executed directly in the ".Yes" interaction
        // whereas the .gosub statements just push ranges, which results in later execution
        // yes-statement has to be executed after the last .gosub range has been popped from the stack
    }
}
