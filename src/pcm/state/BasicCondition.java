package pcm.state;

import static pcm.state.StateKeywords.Applied;
import static pcm.state.StateKeywords.To;

import java.util.function.BiFunction;

import pcm.controller.Player;
import pcm.model.AbstractAction.Statement;
import pcm.model.ConditionRange;
import pcm.state.persistence.ScriptState;
import teaselib.State;

public class BasicCondition implements Condition {
    protected final ParameterizedConditionStatement statement;

    public BasicCondition(ParameterizedConditionStatement statement) {
        this.statement = statement;
    }

    @Override
    public boolean isTrueFor(ScriptState state) {
        return statement.call(state);
    }

    @Override
    public boolean isInside(ConditionRange conditionRange) {
        return conditionRange.contains(this);
    }

    protected static ParameterizedConditionStatement applied(StateCommandLineParameters args, String[] items,
            Statement statement, BiFunction<Player, String, State> stateSupplier) {
        String[] peers = args.optionalPeers(Applied, To);
        return new ParameterizedConditionStatement(statement, args) {
            @Override
            public boolean call(ScriptState scriptState) {
                for (String value : items) {
                    var state = stateSupplier.apply(scriptState.player, value);
                    if (peers.length == 0) {
                        if (!state.applied()) {
                            return false;
                        }
                    } else {
                        for (String peer : peers) {
                            if (!state.is(peer)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        };
    }

}
