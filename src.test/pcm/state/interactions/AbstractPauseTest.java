package pcm.state.interactions;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.visuals.SpokenMessage;
import pcm.state.visuals.SpokenMessage.Entry;
import pcm.util.TestPlayer;
import teaselib.Answer;

import static org.junit.jupiter.api.Assertions.*;

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
        assertInstanceOf(Yes.class, action.interaction);

        SpokenMessage spokenMessage = (SpokenMessage) action.message;
        assertNotNull(spokenMessage);
        assertEquals(2, spokenMessage.getMessages().size());

        List<Entry> entries = spokenMessage.entries();
        assertEquals(2, entries.size());

        Entry first = entries.getFirst();
        assertEquals("Foo.", first.message.get(0).value);
        assertTrue(first.answer.isPresent());
        assertEquals(Answer.Meaning.NO, first.answer.get().meaning);
        assertEquals(Answer.no("No Foo"), first.answer.get());

        Entry second = entries.get(1);
        assertEquals("Bar.", second.message.get(0).value);
        assertFalse(second.answer.isPresent());
    }

    @Test
    public void testScriptStructureWithFinalBrckets()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Action action = createPlayer().script.actions.get(1010);
        assertNotNull(action);
        assertInstanceOf(Range.class, action.interaction);

        SpokenMessage spokenMessage = (SpokenMessage) action.message;
        assertNotNull(spokenMessage);
        assertEquals(2, spokenMessage.getMessages().size());

        List<Entry> entries = spokenMessage.entries();
        assertEquals(2, entries.size());

        Entry first = entries.getFirst();
        assertEquals("Foo.", first.message.get(0).value);
        assertTrue(first.answer.isPresent());
        assertEquals(Answer.Meaning.NO, first.answer.get().meaning);
        assertEquals("No Foo", first.answer.get().text.getFirst());

        Entry second = entries.get(1);
        assertEquals("Bar.", second.message.get(0).value);
        assertTrue(second.answer.isPresent());
        assertEquals(Answer.Meaning.YES, second.answer.get().meaning);
        assertEquals("Yes Bar", second.answer.get().text.getFirst());
    }
}
