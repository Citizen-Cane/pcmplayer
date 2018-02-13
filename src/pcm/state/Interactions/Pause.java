package pcm.state.Interactions;

import pcm.model.AbstractAction.Statement;
import teaselib.Answer;

public class Pause extends AbstractPause {
    public Pause() {
        super(Statement.ResumeText, Answer.Meaning.INDIFFERENT);
    }
}
