package pcm.state.interactions;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import pcm.controller.Player;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.LoadSbdRange;
import pcm.model.Script;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.Interaction;

public class LoadSbd implements Interaction {
    public final LoadSbdRange range;

    public LoadSbd(String script, ActionRange range) {
        this.range = new LoadSbdRange(script, range);
    }

    @Override
    public ActionRange getRange(Player player, Script script, Action action, Runnable visuals)
            throws ScriptExecutionException {
        visuals.run();
        return range;
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        try {
            Script loadSbd = script.load(range.script);
            loadSbd.actions.validate(script, action, range, validationErrors);
        } catch (ValidationIssue e) {
            validationErrors.add(e);
        } catch (IOException e) {
            validationErrors.add(new ValidationIssue(action, e, script));
        }
    }

    @Override
    public List<ActionRange> coverage() {
        return Collections.singletonList(range);
    }

}
