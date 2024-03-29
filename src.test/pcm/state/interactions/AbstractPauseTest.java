package pcm.state.interactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.visuals.SpokenMessage;
import pcm.state.visuals.SpokenMessage.Entry;
import pcm.util.TestPlayer;
import teaselib.Answer;

public class AbstractPauseTest {

    private static Player createPlayer()
            throws IOException, ScriptParsingException, ValidationIssue, ScriptExecutionException {
        TestPlayer player = new TestPlayer(AbstractPauseTest.class);
        player.loadScript(AbstractPauseTest.class.getSimpleName());
        return player;
    }

    @Test
    public void testScriptStructureWithFinalInteraction()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Action action = createPlayer().script.actions.get(1000);
        assertNotNull(action);

        SpokenMessage spokenMessage = (SpokenMessage) action.message;
        assertNotNull(spokenMessage);
        assertEquals(2, spokenMessage.getMessages().size());

        List<Entry> entries = spokenMessage.entries();
        assertEquals(2, entries.size());

        assertEquals("Foo.", entries.get(0).message.get(0).value);

        assertTrue(entries.get(0).answer.isPresent());
        assertEquals(Answer.Meaning.NO, entries.get(0).answer.get().meaning);
        assertEquals(Answer.no("No Foo"), entries.get(0).answer.get());

        assertEquals("Bar.", entries.get(1).message.get(0).value);

        assertFalse(entries.get(1).answer.isPresent());

        assertTrue(action.interaction instanceof Yes);
    }

    @Test
    public void testScriptStructureWithFinalBrckets()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Action action = createPlayer().script.actions.get(1010);
        assertNotNull(action);

        SpokenMessage spokenMessage = (SpokenMessage) action.message;
        assertNotNull(spokenMessage);
        assertEquals(2, spokenMessage.getMessages().size());

        List<Entry> entries = spokenMessage.entries();
        assertEquals(2, entries.size());

        assertEquals("Foo.", entries.get(0).message.get(0).value);

        assertTrue(entries.get(0).answer.isPresent());
        assertEquals(Answer.Meaning.NO, entries.get(0).answer.get().meaning);
        assertEquals("No Foo", entries.get(0).answer.get().text.get(0));

        assertEquals("Bar.", entries.get(1).message.get(0).value);

        assertTrue(entries.get(1).answer.isPresent());
        assertEquals(Answer.Meaning.YES, entries.get(1).answer.get().meaning);
        assertEquals("Yes Bar", entries.get(1).answer.get().text.get(0));

        assertTrue(action.interaction instanceof Range);
    }
}
