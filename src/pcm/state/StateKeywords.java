package pcm.state;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum StateKeywords {
    Item,

    Apply,

    To,
    Over,
    Remember,

    Remove,

    From,

    SetAvailable,

    Is,
    Isnt,
    Available,
    CanApply,
    Applied,
    Free,
    Expired,
    Remaining,
    Elapsed,
    Limit,
    Removed,

    Matching,

    GreaterThan,
    GreaterOrEqualThan,
    LessOrEqualThan,
    LessThan,
    Equals,

    Not,;

    static final Set<StateKeywords> COMMANDS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(Apply, Remove, SetAvailable)));

    static final Set<StateKeywords> ComparisonOperators = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(StateKeywords.GreaterThan, StateKeywords.GreaterOrEqualThan,
                    StateKeywords.LessOrEqualThan, StateKeywords.LessThan, StateKeywords.Equals)));
}
