package pcm.state.visuals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.state.ValidatableResources;
import pcm.state.Visual;
import teaselib.Actor;
import teaselib.Answer;
import teaselib.Message;
import teaselib.MessagePart;

public class SpokenMessage implements Visual, ValidatableResources {
    public static final Set<Statement> MODIFIERS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(Statement.Append, Statement.Prepend, Statement.Replace)));

    private final List<Entry> entries = new ArrayList<>();
    private Message message = null;
    private Statement modifier = null;
    private final Actor actor;

    public static class Entry {
        public final Message message;
        public final Statement modifier;
        public final Optional<Answer> answer;

        public Entry(Message message, Statement modifier) {
            super();
            this.message = message;
            this.modifier = modifier;
            this.answer = Optional.empty();
        }

        public Entry(Message message, Statement modifier, Answer answer) {
            super();
            this.message = message;
            this.modifier = modifier;
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
        if (part.type == Message.Type.Image && !Message.ActorImage.equalsIgnoreCase(part.value)
                && !Message.NoImage.equalsIgnoreCase(part.value)) {
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
        entries.add(new Entry(message, modifier));
    }

    public void completeSection(Answer answer) {
        entries.add(new Entry(message, modifier, answer));
    }

    public void startNewSection() {
        message = null;
        modifier = null;
    }

    public void completeMessage() {
        if (message != null) {
            entries.add(new Entry(message, modifier));
        }
        for (Entry entry : entries) {
            entry.message.joinSentences().readAloud();
        }
    }

    @Override
    public void render(Player player) {
        for (Entry entry : entries) {
            if (entry.modifier == Statement.Append) {
                player.append(entry.message);
            } else if (entry.modifier == Statement.Prepend) {
                player.prepend(entry.message);
            } else if (entry.modifier == Statement.Replace) {
                player.replace(entry.message);
            } else {
                player.say(entry.message);
            }

            Optional<Answer> answer = entry.answer;
            if (answer.isPresent()) {
                player.chat(answer.get());
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

    public void setModifier(Statement statement) {
        modifier = statement;
    }
}
