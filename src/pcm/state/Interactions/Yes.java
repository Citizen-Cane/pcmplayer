package pcm.state.Interactions;

import pcm.model.AbstractAction.Statement;
import teaselib.Answer;

public class Yes extends AbstractPause {
    public Yes() {
        super(Statement.YesText, Answer.Meaning.YES);
    }
}
