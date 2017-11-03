package pcm.state.visuals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pcm.controller.Player;
import pcm.state.ValidatableResources;
import pcm.state.Visual;
import teaselib.Actor;
import teaselib.Message;
import teaselib.core.speechrecognition.SpeechRecognitionResult.Confidence;

public class SpokenMessage implements Visual, ValidatableResources {

    private final List<Entry> entries = new ArrayList<>();
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

    public void add(String line) {
        if (message == null) {
            message = new Message(actor);
        }
        Message.Part part = new Message.Part(line);
        if (part.type == Message.Type.Image) {
            message.add(part.type, Image.IMAGES + part.value);
        } else if (Message.Type.AudioTypes.contains(part.type)) {
            message.add(part.type, Sound.SOUNDS + part.value);
        } else if (part.type == Message.Type.DesktopItem) {
            message.add(part.type, part.value);
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
                player.reply(Confidence.Default.lower(), Collections.singletonList(entry.resumeText));
            }
        }
    }

    public List<Message> getMessages() {
        List<Message> messages = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            messages.add(entry.message);
        }
        return messages;
    }

    @Override
    public List<String> resources() {
        List<String> resources = new ArrayList<>();
        for (Entry entry : entries) {
            resources.addAll(entry.message.resources());
        }
        return resources;
    }
}
