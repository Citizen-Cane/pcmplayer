package pcm;

import java.io.IOException;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.util.TestUtils;

public class GosubTest {

    enum Body {
        Chastified
    }

    @Test
    public void testGosubIsExecutedAfterActionCommands()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        pcm.util.TestUtils.play(player, new ActionRange(1000), null);
    }

}
