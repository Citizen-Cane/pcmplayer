package pcm.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pcm.controller.Declarations;
import pcm.model.IllegalStatementException;
import pcm.state.persistence.ScriptState;
import teaselib.core.util.CommandLineParameters;
import teaselib.core.util.QualifiedString;
import teaselib.util.DurationFormat;
import teaselib.util.Items;

public class StateCommandLineParameters extends CommandLineParameters<StateKeywords> {
    private static final long serialVersionUID = 1L;

    private final Declarations declarations;

    public boolean isCommand() {
        for (StateKeywords keyword : StateKeywords.COMMANDS) {
            if (containsKey(keyword)) {
                return true;
            }
        }
        return false;
    }

    public StateCommandLineParameters(CommandLineParameters<StateKeywords> args, Declarations declarations) {
        super(args, StateKeywords.class);
        this.declarations = declarations;
        validateArgs();
    }

    public StateCommandLineParameters(String[] args, Declarations declarations) {
        this(Arrays.asList(args), declarations);
    }

    public StateCommandLineParameters(List<String> args, Declarations declarations) {
        super(normalizedArgs(args), StateKeywords.values(), StateKeywords.class);
        this.declarations = declarations;
        validateArgs();
    }

    private void validateArgs() {
        if (items(StateKeywords.Item).length == 0) {
            throw new IllegalArgumentException("Items must be specified first");
        } else if (items(StateKeywords.Remove).length > 0) {
            throw new IllegalArgumentException("Remove can only remove all items");
        }
    }

    public static List<String> normalizedArgs(List<String> args) {
        List<String> normalizedArgs = new ArrayList<>(args.size());

        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg.equalsIgnoreCase(StateKeywords.Is.name())) {
                if (args.get(i + 1).equalsIgnoreCase(StateKeywords.Not.name())) {
                    if (args.get(i + 2).equalsIgnoreCase(StateKeywords.Free.name())) {
                        normalizedArgs.add(StateKeywords.Applied.name().toLowerCase());
                        i += 2;
                    } else if (args.get(i + 2).equalsIgnoreCase(StateKeywords.Applied.name())) {
                        normalizedArgs.add(StateKeywords.Not.name().toLowerCase());
                        normalizedArgs.add(StateKeywords.Applied.name().toLowerCase());
                        i += 2;
                    } else {
                        normalizedArgs.add(StateKeywords.Not.name().toLowerCase());
                        normalizedArgs.add(StateKeywords.Is.name().toLowerCase());
                        i += 1;
                    }
                } else if (args.get(i + 1).equalsIgnoreCase(StateKeywords.Applied.name())) {
                    normalizedArgs.add(StateKeywords.Applied.name().toLowerCase());
                    i += 1;
                } else if (args.get(i + 1).equalsIgnoreCase(StateKeywords.Free.name())) {
                    normalizedArgs.add(StateKeywords.Free.name().toLowerCase());
                    i += 1;
                } else {
                    normalizedArgs.add(arg);
                }
            } else if (arg.equalsIgnoreCase(StateKeywords.Not.name())) {
                if (args.get(i + 1).equalsIgnoreCase(StateKeywords.Free.name())) {
                    normalizedArgs.add(StateKeywords.Applied.name().toLowerCase());
                    i += 1;
                } else if (args.get(i + 1).equalsIgnoreCase(StateKeywords.Is.name())) {
                    if (args.get(i + 2).equalsIgnoreCase(StateKeywords.Free.name())) {
                        normalizedArgs.add(StateKeywords.Applied.name().toLowerCase());
                        i += 2;
                    } else {
                        normalizedArgs.add(arg);
                    }
                } else {
                    normalizedArgs.add(arg);
                }
            } else if (arg.equalsIgnoreCase(StateKeywords.Isnt.name())) {
                if (args.get(i + 1).equalsIgnoreCase(StateKeywords.Applied.name())) {
                    normalizedArgs.add(StateKeywords.Not.name().toLowerCase());
                    normalizedArgs.add(StateKeywords.Applied.name().toLowerCase());
                    i += 1;
                } else if (args.get(i + 1).equalsIgnoreCase(StateKeywords.Free.name())) {
                    normalizedArgs.add(StateKeywords.Applied.name().toLowerCase());
                    i += 1;
                } else {
                    normalizedArgs.add(StateKeywords.Not.name().toLowerCase());
                    normalizedArgs.add(StateKeywords.Is.name().toLowerCase());
                }
            } else {
                normalizedArgs.add(arg);
            }
        }
        return normalizedArgs;
    }

    public String[] items(StateKeywords keyword) {
        List<String> items = get(keyword);
        declarations.validate(items);
        String[] array = new String[items.size()];
        return items.toArray(array);
    }

    public String item(StateKeywords keyword) {
        List<String> items = get(keyword);
        declarations.validate(items);
        if (items.size() == 1) {
            return items.get(0);
        } else if (items.isEmpty()) {
            throw new IllegalStatementException("Argument expected for " + keyword, this);
        } else {
            throw new IllegalStatementException("Single argument expected for " + keyword + " but got " + items, this);
        }
    }

    public String[] values(StateKeywords keyword) {
        List<String> items = get(keyword);
        String[] array = new String[items.size()];
        return items.toArray(array);
    }

    public String value(StateKeywords keyword) {
        List<String> values = get(keyword);
        if (values.size() == 1) {
            return values.get(0);
        } else if (values.isEmpty()) {
            throw new IllegalStatementException("Argument expected for " + keyword, this);
        } else {
            throw new IllegalStatementException("Single argument expected for " + keyword + " but got " + values, this);
        }
    }

    public DurationFormat durationOption() {
        return containsKey(StateKeywords.Over) ? new DurationFormat(get(StateKeywords.Over).get(0)) : null;
    }

    public boolean rememberOption() {
        return containsKey(StateKeywords.Remember);
    }

    public StateKeywords getComparisonOperator() {
        for (StateKeywords keyword : StateKeywords.ComparisonOperators) {
            if (containsKey(keyword)) {
                // if (values(keyword).length != 1) {
                // throw new IllegalStatementException("Single argument expected for", keyword.toString(), this);
                // }
                return keyword;
            }
        }
        throw new IllegalStatementException("Comparison operator missing or spelled wrong", this);
    }

    public interface Operator {
        boolean isTrueFor(long n, long m);
    }

    public Operator getOperator(StateKeywords keyword) {
        if (keyword == StateKeywords.GreaterThan) {
            return (n, m) -> n > m;
        } else if (keyword == StateKeywords.GreaterOrEqualThan) {
            return (n, m) -> n >= m;
        } else if (keyword == StateKeywords.LessOrEqualThan) {
            return (n, m) -> n <= m;
        } else if (keyword == StateKeywords.LessThan) {
            return (n, m) -> n < m;
        } else if (keyword == StateKeywords.Equals) {
            return (n, m) -> n == m;
        } else {
            throw new IllegalStatementException("Unsupported comparison Operator", keyword, this);
        }
    }

    public Declarations getDeclarations() {
        return declarations;
    }

    public void replaceWithMatching(String[] items, String[] attributes, ScriptState state) {
        remove(StateKeywords.Matching);
        remove(StateKeywords.Item);
        Items matching = state.player.items(items).matching(attributes).inventory();
        put(StateKeywords.Item, matching.stream().map(QualifiedString::of).map(QualifiedString::toString).toList());
    }

    public String[] optionalPeers(StateKeywords condition, StateKeywords peerList) {
        String[] peers = items(containsKey(peerList) ? peerList : condition);
        if (containsKey(peerList) && peers.length == 0) {
            throw new IllegalArgumentException("Missing peers to " + condition.name().toLowerCase() + " the item '"
                    + peerList.name().toLowerCase() + "'");
        } else if (containsKey(condition) && items(condition).length > 0) {
            throw new IllegalArgumentException("'" + condition.name() + "' just applies the default peers - use '"
                    + condition.name() + " " + peerList.name() + "' to apply additional peers");
        }
        return peers;
    }

}
