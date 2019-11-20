package pcm.state.interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;

public abstract class AbstractBreakInteraction extends AbstractInteraction {
    final Map<Statement, ActionRange> choiceRanges;

    public AbstractBreakInteraction(Map<Statement, ActionRange> choiceRanges) {
        if (choiceRanges.containsKey(Statement.Chat) && choiceRanges.size() > 1) {
            throw new IllegalArgumentException(Statement.Chat.toString());
        }

        this.choiceRanges = choiceRanges;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getClass().getSimpleName() + ": ");
        for (Entry<Statement, ActionRange> entry : choiceRanges.entrySet()) {
            s.append(entry.getKey());
            s.append("=");
            s.append(entry.getValue());
            s.append(" ");
        }
        return s.toString();
    }

    @Override
    public void validate(Script script, Action action, List<ValidationIssue> validationErrors)
            throws ScriptParsingException {
        try {
            for (Entry<Statement, ActionRange> entry : choiceRanges.entrySet()) {
                action.getResponseText(entry.getKey(), script);
            }
        } catch (Exception e) {
            validationErrors.add(new ValidationIssue(script, action, e));
        }

        for (Entry<Statement, ActionRange> entry : choiceRanges.entrySet()) {
            script.actions.validate(script, action, entry.getValue(), validationErrors);
        }

        super.validate(script, action, validationErrors);
    }

    @Override
    public List<ActionRange> coverage() {
        List<ActionRange> rangeProviderCoverage = super.coverage();
        List<ActionRange> coverage = new ArrayList<>(choiceRanges.size() + rangeProviderCoverage.size());
        coverage.addAll(choiceRanges.values());
        coverage.addAll(rangeProviderCoverage);
        return coverage;
    }

}
