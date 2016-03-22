/**
 * 
 */
package pcm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.state.visuals.SpokenMessage;
import teaselib.Actor;
import teaselib.Message;
import teaselib.Mood;
import teaselib.TeaseLib;
import teaselib.core.ResourceLoader;
import teaselib.core.texttospeech.Voice;
import teaselib.hosts.DummyHost;
import teaselib.hosts.DummyPersistence;

/**
 * @author someone
 *
 */
public class MessageTests {

    Player player = new Player(
            TeaseLib.init(new DummyHost(), new DummyPersistence()),
            new ResourceLoader(this.getClass()),
            new Actor(Actor.Dominant, Voice.Gender.Female, "en-us"), "pcm",
            null) {

        @Override
        public void run() {
        }
    };

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUpBefore() throws Exception {
        player.loadScript("MessageTests");
    }

    private Message getMessageOfAction(int n) {
        Action action = player.script.actions.get(n);
        Message message = ((SpokenMessage) action.visuals
                .get(Statement.Message)).getMessages().get(0);
        return message;
    }

    @Test
    public void testReadAloudInBetween() {
        Message message = getMessageOfAction(1000);
        // Leading and trailing white space before is okay
        assertEquals(7, message.getParts().size());
        assertEquals(Message.Type.Text, message.getParts().get(0).type);
        assertEquals(Message.Type.Text, message.getParts().get(1).type);
        assertEquals(Message.Type.Text, message.getParts().get(2).type);
        assertEquals(Message.Type.Mood, message.getParts().get(3).type);
        assertEquals(Mood.Reading, message.getParts().get(3).value);
        assertEquals(Message.Type.Text, message.getParts().get(4).type);
        assertEquals(Message.Type.Mood, message.getParts().get(5).type);
        assertEquals(Mood.Neutral, message.getParts().get(5).value);
        assertEquals(Message.Type.Text, message.getParts().get(6).type);
    }

    @Test
    public void testReadAloudMultipleLines() {
        Message message = getMessageOfAction(1001);
        // Leading and trailing white space before is okay
        assertEquals(11, message.getParts().size());
        assertEquals(Message.Type.Mood, message.getParts().get(0).type);
        assertEquals(Mood.Reading, message.getParts().get(0).value);
        assertEquals(Message.Type.Text, message.getParts().get(1).type);
        assertEquals(Message.Type.Mood, message.getParts().get(2).type);
        assertEquals(Mood.Reading, message.getParts().get(2).value);
        assertEquals(Message.Type.Text, message.getParts().get(3).type);
        assertEquals(Message.Type.Mood, message.getParts().get(4).type);
        assertEquals(Mood.Reading, message.getParts().get(4).value);
        assertEquals(Message.Type.Text, message.getParts().get(5).type);
        assertEquals(Message.Type.Mood, message.getParts().get(6).type);
        assertEquals(Mood.Reading, message.getParts().get(6).value);
        assertEquals(Message.Type.Text, message.getParts().get(7).type);
        assertEquals(Message.Type.Mood, message.getParts().get(8).type);
        assertEquals(Mood.Reading, message.getParts().get(8).value);
        assertEquals(Message.Type.Text, message.getParts().get(9).type);
        assertEquals(Message.Type.Mood, message.getParts().get(10).type);
        assertEquals(Mood.Neutral, message.getParts().get(10).value);
    }

    @Test
    public void testReadAloundAndConcatenateParts() {
        Message message = getMessageOfAction(1002);
        // Leading and trailing white space before is okay
        assertEquals(9, message.getParts().size());
        assertEquals(Message.Type.Mood, message.getParts().get(0).type);
        assertEquals(Mood.Reading, message.getParts().get(0).value);
        assertEquals(Message.Type.Text, message.getParts().get(1).type);
        assertEquals(Message.Type.Mood, message.getParts().get(2).type);
        assertEquals(Mood.Reading, message.getParts().get(2).value);
        assertEquals(Message.Type.Text, message.getParts().get(3).type);
        assertEquals(Message.Type.Mood, message.getParts().get(4).type);
        assertEquals(Mood.Reading, message.getParts().get(4).value);
        assertEquals(Message.Type.Text, message.getParts().get(5).type);
        assertEquals(Message.Type.Mood, message.getParts().get(6).type);
        assertEquals(Mood.Reading, message.getParts().get(6).value);
        assertEquals(Message.Type.Text, message.getParts().get(7).type);
        assertEquals(Message.Type.Mood, message.getParts().get(8).type);
        assertEquals(Mood.Neutral, message.getParts().get(8).value);
    }

    @Test
    public void testConcatenate() {
        Message message = getMessageOfAction(1003);
        // Leading and trailing white space before is okay
        assertEquals(1, message.getParts().size());
        assertEquals(Message.Type.Text, message.getParts().get(0).type);
    }

    @Test
    public void testConcatenateMultiple() {
        Message message = getMessageOfAction(1004);
        // Leading and trailing white space before is okay
        assertEquals(4, message.getParts().size());
        assertEquals(Message.Type.Text, message.getParts().get(0).type);
        assertEquals(Message.Type.Text, message.getParts().get(1).type);
        assertEquals(Message.Type.Text, message.getParts().get(2).type);
        assertEquals(Message.Type.Text, message.getParts().get(3).type);
        assertEquals("Little cunt!", message.getParts().get(0).value);
        assertEquals(
                "It surely doesn't take so long to answer such a simple question and I've had enough of your lazy and sluggish attitude.",
                message.getParts().get(1).value);
        assertEquals(
                "You can come back in an hour or so when you're ready to pay attention to your Mistress!",
                message.getParts().get(2).value);
        assertEquals(
                "And for your own good, you'll better stay bare naked that hour.",
                message.getParts().get(3).value);
    }
}
