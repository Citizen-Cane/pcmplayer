package pcm.state.visuals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pcm.controller.Player;
import pcm.state.ValidatableResources;
import pcm.state.Visual;
import teaselib.Actor;
import teaselib.Answer;
import teaselib.Message;
import teaselib.MessagePart;
import teaselib.core.speechrecognition.SpeechRecognitionResult.Confidence;

public class SpokenMessage implements Visual, ValidatableResources {

    private final List<Entry> entries = new ArrayList<>();
    private Message message = null;
    private final Actor actor;

    public static class Entry {
        public final Message message;
        public final Optional<Answer> answer;

        public Entry(Message message) {
            super();
            this.message = message;
            this.answer = Optional.empty();
        }

        public Entry(Message message, Answer answer) {
            super();
            this.message = message;
            this.answer = Optional.of(answer);
        }
    }

    public SpokenMessage(Actor actor) {
        this.actor = actor;
    }

    public void add(String line) {
        if (message == null) {
            message = new Message(actor);
        }
        MessagePart part = new MessagePart(line);
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
        entries.add(new Entry(message));
    }

    public void completeSection(Answer answer) {
        entries.add(new Entry(message, answer));
        message = null;
    }

    public void startNewSection() {
        message = null;
    }

    public void completeMessage() {
        if (message != null) {
            entries.add(new Entry(message));
        }
        for (Entry entry : entries) {
            entry.message.joinSentences().readAloud();
        }
    }

    @Override
    public void render(Player player) {
        for (Entry entry : entries) {
            player.say(entry.message);
            if (entry.answer.isPresent()) {
                player.reply(Confidence.Default.lower(), entry.answer.get());
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

    public List<Entry> entries() {
        return entries;
    }
}
