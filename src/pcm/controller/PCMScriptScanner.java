package pcm.controller;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.Script;
import pcm.state.Visual;
import teaselib.Actor;
import teaselib.text.Message;
import teaselib.texttospeech.ScriptScanner;

public class PCMScriptScanner implements ScriptScanner {

    private Script script;
    private Actor actor;

    public PCMScriptScanner(Script script, Actor actor) {
        this.script = script;
        this.actor = actor;
    }

    @Override
    public String getScriptName() {
        return script.name;
    }

    @Override
    public Iterator<Message> iterator() {
        Vector<Message> r = new Vector<>();
        for (Action action : script.actions.values()) {
            Map<Statement, Visual> visuals = action.visuals;
            if (visuals != null) {
                if (visuals.containsKey(Statement.Message)) {
                    Visual visual = visuals.get(Statement.Message);
                    if (visual instanceof pcm.state.visuals.SpokenMessage) {
                        pcm.state.visuals.SpokenMessage message = (pcm.state.visuals.SpokenMessage) visual;
                        for (List<String> part : message.getParts()) {
                            r.add(new Message(actor, part));
                        }
                    } else {
                        // It's NoMessage, and there's nothing to do
                    }
                }
            }
        }
        return r.iterator();
    }
}
