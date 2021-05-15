package pcm.state.interactions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.model.Script;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import teaselib.Answer;
import teaselib.Answers;

public abstract class AbstractBreakInteraction extends AbstractInteraction {
    final Map<Statement, ActionRange> choiceRanges;

    protected AbstractBreakInteraction(Map<Statement, ActionRange> choiceRanges) {
        if (choiceRanges.containsKey(Statement.Chat) && choiceRanges.size() > 1) {
            throw new IllegalArgumentException(Statement.Chat.toString());
        }

        this.choiceRanges = choiceRanges;
    }

    Map<Answer, ActionRange> ranges(Script script, Action action) {
        Map<Answer, ActionRange> ranges = new LinkedHashMap<>();
        for (Entry<Statement, ActionRange> entry : choiceRanges.entrySet()) {
            String choice = action.getResponseText(entry.getKey(), script);
            Answer answer;
            if (entry.getKey() == Statement.YesText) {
                answer = Answer.yes(choice);
            } else if (entry.getKey() == Statement.NoText) {
                answer = Answer.no(choice);
            } else {
                answer = Answer.resume(choice);
            }
            ranges.put(answer, entry.getValue());
        }
        return ranges;
    }

    static Answers answers(Map<Answer, ActionRange> ranges) {
        return new Answers(new ArrayList<>(ranges.keySet()));
    }

    @Override
    public String toString() {
        var s = new StringBuilder();
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
