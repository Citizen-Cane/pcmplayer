package pcm.state.interactions;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.visuals.SpokenMessage;
import pcm.state.visuals.SpokenMessage.Entry;
import pcm.util.TestPlayer;
import teaselib.Answer;

import static org.junit.jupiter.api.Assertions.*;

public class YesNoTest {

    final Player player;

    public YesNoTest() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        player = TestPlayer.loadScript(getClass());
    }

    @Test
    public void testScriptStructureWithFinalInteraction() {
        Action action = player.script.actions.get(1000);
        assertNotNull(action);

        SpokenMessage spokenMessage = (SpokenMessage) action.message;
        assertNotNull(spokenMessage);
        assertEquals(2, spokenMessage.getMessages().size());

        List<Entry> entries = spokenMessage.entries();
        assertEquals(2, entries.size());

        Entry first = entries.getFirst();
        assertEquals("Foo.", first.message.get(0).value);
        assertTrue(first.answer.isPresent());
        assertEquals(Answer.no("No Foo"), first.answer.get());

        Entry second = entries.get(1);
        assertEquals("Bar.", second.message.get(0).value);
        assertFalse(second.answer.isPresent());

        testYesNo(action);
    }

    @Test
    public void testScriptStructureWithFinalBrackets() {
        Action action = player.script.actions.get(1010);
        assertNotNull(action);

        SpokenMessage spokenMessage = (SpokenMessage) action.message;
        assertNotNull(spokenMessage);
        assertEquals(2, spokenMessage.getMessages().size());

        List<Entry> entries = spokenMessage.entries();
        testChat(entries);
        testYesNo(action);
    }

    private static void testChat(List<Entry> entries) {
        assertEquals(2, entries.size());

        Entry first = entries.getFirst();
        assertEquals("Foo.", first.message.get(0).value);

        assertTrue(first.answer.isPresent());
        assertEquals(Answer.no("No Foo"), first.answer.get());

        Entry second = entries.get(1);
        assertEquals("Bar.", second.message.get(0).value);

        assertTrue(second.answer.isPresent());
        assertEquals(Answer.yes("Yes Bar"), second.answer.get());
    }

    private static void testYesNo(Action action) {
        assertInstanceOf(YesNo.class, action.interaction);
        assertEquals("YesNo Yes Foo", action.getResponseText(Statement.YesText, null));
        assertEquals("YesNo No Bar", action.getResponseText(Statement.NoText, null));
    }

}
