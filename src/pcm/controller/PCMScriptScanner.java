package pcm.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pcm.model.Action;
import pcm.model.Script;
import pcm.state.Visual;
import teaselib.Message;
import teaselib.core.texttospeech.ScriptScanner;

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
        List<Message> r = new ArrayList<>();
        for (Action action : script.actions.values()) {
            Visual visual = action.message;
            if (visual instanceof pcm.state.visuals.SpokenMessage) {
                pcm.state.visuals.SpokenMessage spokenMessage = (pcm.state.visuals.SpokenMessage) visual;
                for (Message message : spokenMessage.getMessages()) {
                    r.add(message);
                }
            } else {
                // It's NoMessage, and there's nothing to do
            }
        }
        return r.iterator();
    }
}
