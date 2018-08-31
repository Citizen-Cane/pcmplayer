/**
 * 
 */
package pcm;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.state.visuals.SpokenMessage;
import pcm.util.TestUtils;
import teaselib.Message;
import teaselib.Mood;

/**
 * @author Citizen-Cane
 *
 */
public class MessageTests {

    Player player = TestUtils.createPlayer(getClass());

    public MessageTests() throws IOException {
    }

    @Before
    public void setUpBefore() throws Exception {
        player.loadScript("MessageTests");
    }

    private Message getMessageOfAction(int n) {
        Action action = player.script.actions.get(n);
        return ((SpokenMessage) action.message).getMessages().get(0);
    }

    @Test
    public void testReadAloudInBetween() {
        Message message = getMessageOfAction(1000);
        // Leading and trailing white space before is okay
        assertEquals(7, message.size());
        assertEquals(Message.Type.Text, message.get(0).type);
        assertEquals(Message.Type.Text, message.get(1).type);
        assertEquals(Message.Type.Text, message.get(2).type);
        assertEquals(Message.Type.Mood, message.get(3).type);
        assertEquals(Mood.Reading, message.get(3).value);
        assertEquals(Message.Type.Text, message.get(4).type);
        assertEquals(Message.Type.Mood, message.get(5).type);
        assertEquals(Mood.Neutral, message.get(5).value);
        assertEquals(Message.Type.Text, message.get(6).type);
    }

    @Test
    public void testReadAloudMultipleLines() {
        Message message = getMessageOfAction(1001);
        // Leading and trailing white space before is okay
        assertEquals(11, message.size());
        assertEquals(Message.Type.Mood, message.get(0).type);
        assertEquals(Mood.Reading, message.get(0).value);
        assertEquals(Message.Type.Text, message.get(1).type);
        assertEquals(Message.Type.Mood, message.get(2).type);
        assertEquals(Mood.Reading, message.get(2).value);
        assertEquals(Message.Type.Text, message.get(3).type);
        assertEquals(Message.Type.Mood, message.get(4).type);
        assertEquals(Mood.Reading, message.get(4).value);
        assertEquals(Message.Type.Text, message.get(5).type);
        assertEquals(Message.Type.Mood, message.get(6).type);
        assertEquals(Mood.Reading, message.get(6).value);
        assertEquals(Message.Type.Text, message.get(7).type);
        assertEquals(Message.Type.Mood, message.get(8).type);
        assertEquals(Mood.Reading, message.get(8).value);
        assertEquals(Message.Type.Text, message.get(9).type);
        assertEquals(Message.Type.Mood, message.get(10).type);
        assertEquals(Mood.Neutral, message.get(10).value);
    }

    @Test
    public void testReadAloundAndConcatenateParts() {
        Message message = getMessageOfAction(1002);
        // Leading and trailing white space before is okay
        assertEquals(9, message.size());
        assertEquals(Message.Type.Mood, message.get(0).type);
        assertEquals(Mood.Reading, message.get(0).value);
        assertEquals(Message.Type.Text, message.get(1).type);
        assertEquals(Message.Type.Mood, message.get(2).type);
        assertEquals(Mood.Reading, message.get(2).value);
        assertEquals(Message.Type.Text, message.get(3).type);
        assertEquals(Message.Type.Mood, message.get(4).type);
        assertEquals(Mood.Reading, message.get(4).value);
        assertEquals(Message.Type.Text, message.get(5).type);
        assertEquals(Message.Type.Mood, message.get(6).type);
        assertEquals(Mood.Reading, message.get(6).value);
        assertEquals(Message.Type.Text, message.get(7).type);
        assertEquals(Message.Type.Mood, message.get(8).type);
        assertEquals(Mood.Neutral, message.get(8).value);
    }

    @Test
    public void testConcatenate() {
        Message message = getMessageOfAction(1003);
        // Leading and trailing white space before is okay
        assertEquals(1, message.size());
        assertEquals(Message.Type.Text, message.get(0).type);
    }

    @Test
    public void testConcatenateMultiple() {
        Message message = getMessageOfAction(1004);
        // Leading and trailing white space before is okay
        assertEquals(4, message.size());
        assertEquals(Message.Type.Text, message.get(0).type);
        assertEquals(Message.Type.Text, message.get(1).type);
        assertEquals(Message.Type.Text, message.get(2).type);
        assertEquals(Message.Type.Text, message.get(3).type);
        assertEquals("Little cunt!", message.get(0).value);
        assertEquals(
                "It surely doesn't take so long to answer such a simple question and I've had enough of your lazy and sluggish attitude.",
                message.get(1).value);
        assertEquals("You can come back in an hour or so when you're ready to pay attention to your Mistress!",
                message.get(2).value);
        assertEquals("And for your own good, you'll better stay bare naked that hour.", message.get(3).value);
    }

    @Test
    public void testTextParts() {
        Message message = getMessageOfAction(1005);
        assertEquals(3, message.size());

        Message message2 = getMessageOfAction(1006);
        assertEquals(2, message2.size());
    }

    @Test
    public void testTextPartsWithImage() {
        Message message = getMessageOfAction(1007);
        assertEquals(3, message.size());

        Message message2 = getMessageOfAction(1008);
        assertEquals(3, message2.size());
    }
}
