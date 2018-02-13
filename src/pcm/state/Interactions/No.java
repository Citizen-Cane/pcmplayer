package pcm.state.Interactions;

import pcm.model.AbstractAction.Statement;
import teaselib.Answer;

public class No extends AbstractPause {
    public No() {
        super(Statement.NoText, Answer.Meaning.NO);
    }
}
