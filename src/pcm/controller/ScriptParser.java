package pcm.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.Script;
import pcm.model.ScriptLineTokenizer;
import pcm.model.ScriptParsingException;
import pcm.model.Symbols;
import pcm.model.ValidationIssue;
import pcm.state.interactions.AbstractPause;
import pcm.state.visuals.SpokenMessage;
import pcm.state.visuals.Txt;
import teaselib.Actor;

public class ScriptParser {
    private final static String ACTIONMATCH = "[action ";
    private final static String COMMENT = "'";
    private static final String DEFINE_IF = "#if";
    private static final String DEFINE_ELSEIF = "#elseif";
    private static final String DEFINE_ELSE = "#else";
    private static final String DEFINE_ENDIF = "#endif";

    private final Symbols staticSymbols;
    private final Deque<String[]> preprocessorScope = new ArrayDeque<>();
    private final BufferedReader reader;

    private final Map<String, String> defines = new LinkedHashMap<>();
    private final Declarations declarations;

    private String line = null;
    private int l = 0;
    private int n = 0;
    private int previousActionNumber = 0;

    public ScriptParser(BufferedReader reader, Symbols staticSymbols) {
        this.reader = reader;
        this.staticSymbols = staticSymbols;
        this.declarations = new Declarations();
    }

    public void parse(Script script) throws ScriptParsingException, IOException {
        while ((line = readLine()) != null) {
            if (line.toLowerCase().startsWith(ACTIONMATCH)) {
                return;
            } else if (line.startsWith(".")) {
                try {
                    ScriptLineTokenizer cmd = new ScriptLineTokenizer(l, applyDefines(line), script, declarations);
                    if (cmd.statement == Statement.Define) {
                        defines.put(cmd.args()[0], cmd.argsFrom(1));
                    } else if (cmd.statement == Statement.Declare) {
                        declarations.add(cmd.args()[0], cmd.args()[1]);
                    } else {
                        script.add(cmd);
                    }
                } catch (UnsupportedOperationException e) {
                    throw new ScriptParsingException(l, n, line, e.getMessage(), script);
                } catch (Throwable t) {
                    throw new ScriptParsingException(l, n, line, t, script);
                }
            } else {
                throw new ScriptParsingException(l, n, line, "Unexpected script input", script);
            }
        }
        return;
    }

    private String applyDefines(String string) {
        for (Entry<String, String> entry : defines.entrySet()) {
            string = string.replace(entry.getKey(), entry.getValue());
        }
        return string;
    }

    public Action parseAction(Script script) throws ScriptParsingException, ValidationIssue {
        if (line == null) {
            return null;
        } else {
            Action action = null;
            try {
                int start = ACTIONMATCH.length();
                int end = line.indexOf(']');
                if (end < start) {
                    throw new ScriptParsingException(l, 0, line, "Invalid action number", script);
                }
                n = Integer.parseInt(line.substring(start, end));
                if (n <= previousActionNumber) {
                    throw new ScriptParsingException(l, n, line, "Action must be defined in increasing order", script);
                } else {
                    action = new Action(n);
                    previousActionNumber = action.number;

                    StatementCollectors.Factory collectorFactory = getCollectorFactory(script.actor);
                    StatementCollectors collectors = new StatementCollectors(collectorFactory);

                    SpokenMessage message = null;
                    while ((line = readLine()) != null) {
                        // Start of a new action
                        if (line.toLowerCase().startsWith(ACTIONMATCH)) {
                            break;
                        } else if (line.startsWith("[]")) {
                            completeMessageSectionAndStartNew(script, action, message);
                        } else if (line.startsWith("[") && line.endsWith("]")) {
                            handleDeprecatedInlineReply();
                        } else if (line.startsWith(".")) {
                            parseStatement(script, action, collectors);
                        } else {
                            message = parseMessage(script, message);
                        }
                    }
                    finalizeActionParsing(script, action, message, collectors);
                }
            } catch (ScriptParsingException | ValidationIssue e) {
                if (e.script == null) {
                    e.script = script;
                }
                throw e;
            } catch (Exception e) {
                throw new ScriptParsingException(l, n, line, e, script);
            }
            return action;
        }
    }

    protected StatementCollectors.Factory getCollectorFactory(Actor actor) {
        StatementCollectors.Factory collectorFactory = new StatementCollectors.Factory();
        Supplier<StatementCollector> txtCollector = () -> {
            return new StatementCollector() {
                Txt txt;

                @Override
                public void init() {
                    txt = new Txt(actor);
                }

                @Override
                public void parse(ScriptLineTokenizer cmd) {
                    String text = cmd.line.substring(Statement.Txt.toString().length() + 1);
                    txt.add(text.trim());
                }

                @Override
                public void applyTo(Action action) {
                    txt.end();
                    action.addVisual(Statement.Txt, txt);
                }
            };
        };
        collectorFactory.add(Statement.Txt, txtCollector);
        return collectorFactory;
    }

    private void completeMessageSectionAndStartNew(Script script, Action action, SpokenMessage message)
            throws ScriptParsingException {
        if (message != null) {
            if (action.interaction instanceof AbstractPause) {
                AbstractPause pause = (AbstractPause) action.interaction;
                message.completeSection(pause.answer);
                action.interaction = null;
            } else if (action.interaction == null) {
                message.completeSection();
            } else {
                throw new ScriptParsingException(l, n, line, "Interaction "
                        + action.interaction.getClass().getSimpleName() + " not supported for in-action prompts",
                        script);
            }
            message.startNewSection();
        } else {
            throw new ScriptParsingException(l, n, line, "No message before answer", script);
        }
    }

    private static void handleDeprecatedInlineReply() {
        throw new IllegalArgumentException("Deprecated bracket prompt");
    }

    private void parseStatement(Script script, Action action, StatementCollectors collectors)
            throws ScriptParsingException {
        ScriptLineTokenizer cmd = new ScriptLineTokenizer(l, applyDefines(line), script, declarations);
        if (collectors.contains(cmd.statement)) {
            StatementCollector collector = collectors.get(cmd.statement);
            collector.parse(cmd);
        } else {
            try {
                action.add(cmd);
            } catch (ScriptParsingException e) {
                if (e.getCause() != null) {
                    throw new ScriptParsingException(l, n, line, e.getCause(), script);
                } else {
                    throw new ScriptParsingException(l, n, line, e.getMessage(), script);
                }
            }
        }
    }

    private SpokenMessage parseMessage(Script script, SpokenMessage message) {
        if (message == null) {
            message = new SpokenMessage(script.actor);
        }
        message.add(line);
        return message;
    }

    private static void finalizeActionParsing(Script script, Action action, SpokenMessage message,
            StatementCollectors collectors) throws ValidationIssue {
        // Add message to visuals as the last item, because
        // rendering the message triggers rendering of all other
        // visuals
        if (message != null) {
            message.completeMessage();
            action.addVisual(Statement.Message, message);
        }
        // .txt messages must be executed last,
        // so this has to be added last
        for (StatementCollector collector : collectors) {
            collector.applyTo(action);
        }

        action.finalizeParsing(script);
    }

    private String readLine() throws IOException {
        String readLine;
        parseScope: while ((readLine = reader.readLine()) != null) {
            l++;
            readLine = readLine.trim();
            if (readLine.isEmpty() || readLine.startsWith(COMMENT)) {
                continue;
            } else if (readLine.startsWith("#")) {
                String[] args = readLine.replace("\t", " ").split(" ");
                String command = args[0].toLowerCase();
                if (DEFINE_IF.equals(command)) {
                    preprocessorScope.push(args);
                    if (parseConditionalBlock(args)) {
                        continue parseScope;
                    } else {
                        if (continueWithNextBLock()) {
                            continue;
                        } else {
                            preprocessorScope.pop();
                        }
                    }
                } else if (DEFINE_ELSEIF.equals(command)) {
                    readLine = consumeScope();
                } else if (DEFINE_ELSE.equals(command)) {
                    readLine = consumeScope();
                } else if (DEFINE_ENDIF.equals(command)) {
                    preprocessorScope.pop();
                }
            } else {
                break;
            }
        }
        return readLine;
    }

    private boolean parseConditionalBlock(String[] args) {
        boolean parseConditionalBlock = false;
        for (int i = 1; i < args.length; i++) {
            if (staticSymbols.containsKey(args[i])) {
                parseConditionalBlock = true;
            }
        }
        return parseConditionalBlock;
    }

    private boolean continueWithNextBLock() throws IOException {
        String readLine = consumeScope(DEFINE_ELSEIF, DEFINE_ELSE);
        String[] args = readLine.replace("\t", " ").split(" ");
        String command = args[0].toLowerCase();
        final boolean continueWithNextBLock;
        if (DEFINE_ELSEIF.equals(command)) {
            // check condition before continuing
            if (parseConditionalBlock(args)) {
                continueWithNextBLock = true;
            } else {
                continueWithNextBLock = continueWithNextBLock();
            }
        } else if (DEFINE_ELSE.equals(command)) {
            continueWithNextBLock = true;
        } else if (DEFINE_ENDIF.equals(command)) {
            continueWithNextBLock = false;
        } else {
            continueWithNextBLock = false;
        }
        return continueWithNextBLock;
    }

    private String consumeScope(String... until) throws IOException {
        String readLine;
        int scope = 1;
        consumeScope: while ((readLine = reader.readLine()) != null && scope > 0) {
            String[] args = readLine.replace("\t", " ").split(" ");
            String command = args[0].toLowerCase();
            if (DEFINE_IF.equalsIgnoreCase(command)) {
                scope++;
            } else {
                if (DEFINE_ENDIF.equalsIgnoreCase(command)) {
                    if (--scope == 0) {
                        break consumeScope;
                    }
                } else {
                    for (String string : until) {
                        if (string.equalsIgnoreCase(command)) {
                            if (scope == 1) {
                                break consumeScope;
                            }
                        }
                    }
                }
            }
        }
        return readLine;
    }
}
