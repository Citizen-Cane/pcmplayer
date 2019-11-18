package pcm.model;

import pcm.state.persistence.ScriptState;

public class ScriptAction extends Action {

    public ScriptAction(int n) {
        super(n);
    }

    @Override
    public void execute(ScriptState state) throws ScriptExecutionException {
        state.set(this);
        super.execute(state);
    }

}
