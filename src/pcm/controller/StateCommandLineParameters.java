package pcm.controller;

import static pcm.controller.StateCommandLineParameters.Keyword.Over;
import static pcm.controller.StateCommandLineParameters.Keyword.Remember;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import pcm.model.DurationFormat;
import teaselib.State;
import teaselib.core.util.CommandLineParameters;

public class StateCommandLineParameters extends CommandLineParameters<StateCommandLineParameters.Keyword> {
    private static final long serialVersionUID = 1L;
    private Declarations declarations;

    public enum Keyword {
        Item,

        Apply,

        To,
        Over,
        Remember,

        Remove,

        From,

        SetAvailable,

        Is,
        Available,
        CanApply,
        Applied,
        Free,
        Expired,
        Remaining,
        Elapsed,

        GreaterThan,
        LessOrEqualThan,

        ;

        static final Set<Keyword> COMMANDS = new HashSet<Keyword>(Arrays.asList(Apply, Remove, SetAvailable));
    }

    public boolean isCommand() {
        for (Keyword keyword : Keyword.COMMANDS) {
            if (containsKey(keyword)) {
                return true;
            }
        }
        return false;
    }

    public StateCommandLineParameters(String[] args, Declarations declarations) {
        this(Arrays.asList(args), declarations);
    }

    public StateCommandLineParameters(List<String> args, Declarations declarations) {
        super(args, Keyword.values());
        this.declarations = declarations;
        try {
            if (items(Keyword.Item).length == 0) {
                throw new IllegalArgumentException("Items must be specified first");
            } else if (items(Keyword.Remove).length > 0) {
                throw new IllegalArgumentException("Remove can may only remove all items");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Not an enum", e);
        }

    }

    public String[] items(Keyword keyword) throws ClassNotFoundException {
        List<String> items = declarations.checked(get(keyword));
        String[] array = new String[items.size()];
        return items.toArray(array);
    }

    public String item(Keyword keyword) throws ClassNotFoundException {
        List<String> items = declarations.checked(get(keyword));
        if (items.size() == 1) {
            return items.get(0);
        } else {
            throw new IllegalArgumentException(keyword + " requires a single argument");
        }
    }

    public String[] values(Keyword keyword) {
        List<String> items = get(keyword);
        String[] array = new String[items.size()];
        return items.toArray(array);
    }

    public String value(Keyword keyword) {
        List<String> items = get(keyword);
        if (items.size() == 1) {
            return items.get(0);
        } else {
            throw new IllegalArgumentException(keyword + " requires a sinlge argument");
        }
    }

    public DurationFormat durationOption() {
        return containsKey(Over) ? new DurationFormat(get(Keyword.Over).get(0)) : null;
    }

    public boolean rememberOption() {
        return containsKey(Remember);
    }

    public void handleStateOptions(State.Options options, final DurationFormat duration, final boolean remember) {
        if (duration != null) {
            State.Persistence persistence = options.over(duration.toSeconds(), TimeUnit.SECONDS);
            if (remember) {
                persistence.remember();
            }
        } else if (remember) {
            options.remember();
        }
    }

    public Keyword getCondition() {
        Keyword[] conditionOperators = { Keyword.GreaterThan, Keyword.LessOrEqualThan };
        for (Keyword keyword : conditionOperators) {
            if (containsKey(keyword)) {
                if (values(keyword).length != 1) {
                    throw new IllegalArgumentException(
                            "Condition operator " + keyword.toString() + " expects a single argument");
                }
                return keyword;
            }
        }
        throw new IllegalArgumentException("Comparison operator missing ");
    }

    public interface Operator {
        boolean isTrueFor(long arg0, long arg1);
    }

    public Operator getOperator(Keyword keyword) {
        if (keyword == Keyword.GreaterThan) {
            return new Operator() {
                @Override
                public boolean isTrueFor(long arg0, long arg1) {
                    return arg0 > arg1;
                }
            };
        } else if (keyword == Keyword.LessOrEqualThan) {
            return new Operator() {
                @Override
                public boolean isTrueFor(long arg0, long arg1) {
                    return arg0 <= arg1;
                }
            };
        } else {
            throw new IllegalArgumentException("Operator " + keyword.toString() + " not supported");
        }
    }
}
