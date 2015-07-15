package pcm.controller;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.Script;
import pcm.state.Visual;
import teaselib.text.Message;
import teaselib.texttospeech.ScriptScanner;

public class PCMScriptScanner implements ScriptScanner {

    private Script script;

    public PCMScriptScanner(Script script) {
        this.script = script;
    }

    @Override
    public String getScriptName() {
        return script.name;
    }

    @Override
    public Iterator<Message> iterator() {
        Vector<Message> r = new Vector<Message>();
        for (Action action : script.actions.values()) {
            Map<Statement, Visual> visuals = action.visuals;
            if (visuals != null) {
                if (visuals.containsKey(Statement.Message)) {
                    Visual visual = visuals.get(Statement.Message);
                    if (visual instanceof pcm.state.visuals.SpokenMessage) {
                        pcm.state.visuals.SpokenMessage spokenMessage = (pcm.state.visuals.SpokenMessage) visual;
                        for (Message message : spokenMessage.getParts()) {
                            r.add(message);
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
