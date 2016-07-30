package pcm.state.visuals;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.Script;
import pcm.model.ValidationIssue;
import pcm.state.Validatable;
import pcm.state.Visual;
import teaselib.Actor;
import teaselib.Message;

public class SpokenMessage implements Visual, Validatable {

    private final List<Entry> entries = new Vector<Entry>();
    private Message message = null;
    private final Actor actor;

    private static class Entry {
        final Message message;
        final String resumeText;

        public Entry(Message message, String resumeText) {
            super();
            this.message = message;
            this.resumeText = resumeText;
        }
    }

    public SpokenMessage(Actor actor) {
        this.actor = actor;
    }

    public void add(String line, String resourcePath) {
        if (message == null) {
            message = new Message(actor);
        }
        Message.Part part = new Message.Part(line);
        if (part.type == Message.Type.Image) {
            message.add(part.type, resourcePath + Image.IMAGES + part.value);
        } else if (Message.Type.AudioTypes.contains(part.type)) {
            message.add(part.type, resourcePath + Sound.SOUNDS + part.value);
        } else if (part.type == Message.Type.DesktopItem) {
            message.add(part.type, resourcePath + part.value);
        } else {
            message.add(part);
        }
    }

    public void completeSection() {
        entries.add(new Entry(message, null));
    }

    public void completeSection(String resumeText) {
        entries.add(new Entry(message, resumeText));
        message = null;
    }

    public void startNewSection() {
        message = null;
    }

    public void completeMessage() {
        if (message != null) {
            entries.add(new Entry(message, null));
        }
        for (Entry entry : entries) {
            entry.message.joinSentences().readAloud();
        }
    }

    @Override
    public void render(Player player) {
        for (Entry entry : entries) {
            player.say(entry.message);
            if (entry.resumeText != null) {
                player.reply(entry.resumeText);
            }
        }
    }

    public List<Message> getMessages() {
        List<Message> messages = new ArrayList<Message>(entries.size());
        for (Entry entry : entries) {
            messages.add(entry.message);
        }
        return messages;
    }

    @Override
    public void validate(Script script, Action action,
            List<ValidationIssue> validationErrors) {
        // Nothing to do anymore, since all messages have been parsed already
    }
}
