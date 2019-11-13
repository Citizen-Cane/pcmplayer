package pcm.state.interactions;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionLoadSbd;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;

public class LoadSbd implements Interaction {
    public final String scriptName;
    public final int start;
    public final int end;

    public LoadSbd(String script, int start) {
        this(script, start, start);
    }

    public LoadSbd(String script, int start, int end) {
        this.scriptName = script;
        this.start = start;
        this.end = end;
    }

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        visuals.run();
        Script loadSbd;
        try {
            loadSbd = script.load(scriptName);
        } catch (ScriptParsingException e) {
            throw new ScriptExecutionException(action, "Failed to parse script " + scriptName, e, script);
        } catch (ValidationIssue e) {
            throw new ScriptExecutionException(action, "Failed to validate script " + scriptName, e, script);
        } catch (IOException e) {
            throw new ScriptExecutionException(action, "Failed to load script " + scriptName, e, script);
        }
        return new ActionLoadSbd(loadSbd, start, end);
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        try {
            Script loadSbd = script.load(scriptName);
            loadSbd.actions.validate(script, action, new ActionRange(start, end), validationErrors);
        } catch (ValidationIssue e) {
            validationErrors.add(e);
        } catch (IOException e) {
            validationErrors.add(new ValidationIssue(action, e, script));
        }
    }

    @Override
    public List<ActionRange> coverage() {
        // TODO add script reference or name to action range
        return Collections.singletonList(new ActionRange(start, end));
    }

}
