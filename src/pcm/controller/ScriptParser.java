package pcm.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.Script;
import pcm.model.ScriptAction;
import pcm.model.ScriptLineTokenizer;
import pcm.model.ScriptParsingException;
import pcm.model.StatementCollector;
import pcm.model.StatementCollectors;
import pcm.model.Symbols;
import pcm.model.ValidationIssue;
import pcm.state.interactions.AbstractPause;

public class ScriptParser {
    private static final String ACTIONMATCH = "[action ";
    private static final String COMMENT = "'";
    private static final String DEFINE_IF = "#if";
    private static final String DEFINE_ELSEIF = "#elseif";
    private static final String DEFINE_ELSE = "#else";
    private static final String DEFINE_ENDIF = "#endif";

    private final Symbols staticSymbols;
    private final Deque<String[]> preprocessorScope = new ArrayDeque<>();
    private final BufferedReader reader;
    private ScriptCache scriptCache;

    private final Map<String, String> defines;
    private final Declarations declarations;

    private String line = null;
    private int lineNumber = 0;
    private int n = 0;
    private int previousActionNumber = 0;

    public ScriptParser(BufferedReader reader, Symbols staticSymbols, ScriptCache scriptCache) {
        this.reader = reader;
        this.staticSymbols = staticSymbols;
        this.scriptCache = scriptCache;
        this.defines = new LinkedHashMap<>();
        this.declarations = new Declarations();
    }

    public ScriptParser(BufferedReader reader, ScriptParser parent) {
        this.reader = reader;
        this.staticSymbols = parent.staticSymbols;
        this.scriptCache = parent.scriptCache;
        this.defines = parent.defines;
        this.declarations = parent.declarations;
    }

    public void parse(Script script) throws ScriptParsingException, IOException {
        while ((line = readLine()) != null) {
            if (line.toLowerCase().startsWith(ACTIONMATCH)) {
                return;
            } else if (line.startsWith(".")) {
                try {
                    ScriptLineTokenizer cmd = new ScriptLineTokenizer(lineNumber, applyDefines(line), script, declarations);
                    if (cmd.statement == Statement.Define) {
                        defines.put(cmd.args()[0], cmd.argsFrom(1));
                    } else if (cmd.statement == Statement.Declare) {
                        declarations.add(cmd.args()[0], cmd.args()[1]);
                    } else if (cmd.statement == Statement.Include) {
                        parseSubscript(script, cmd);
                    } else {
                        script.add(cmd);
                    }
                } catch (Throwable e) {
                    throw new ScriptParsingException(script, e, lineNumber, line);
                }
            } else {
                throw new ScriptParsingException(script, "Unexpected script input", lineNumber, line);
            }
        }
    }

    private String applyDefines(String string) {
        String replaced = string;
        for (Entry<String, String> entry : defines.entrySet()) {
            replaced = replaced.replace(entry.getKey(), entry.getValue());
        }
        return replaced;
    }

    public Action parseAction(Script script) throws ScriptParsingException, ValidationIssue {
        if (line == null) {
            return null;
        } else {
            try {
                int start = ACTIONMATCH.length();
                int end = line.indexOf(']');
                if (end < start) {
                    throw new ScriptParsingException(script, "Invalid action number", lineNumber, line);
                } else {
                    n = Integer.parseInt(line.substring(start, end));
                    if (n <= previousActionNumber) {
                        throw new ScriptParsingException(script, "Action must be defined in increasing order", lineNumber, line);
                    } else {
                        return parseBody(script);
                    }
                }
            } catch (ScriptParsingException | ValidationIssue e) {
                throw e;
            } catch (Exception e) {
                throw new ScriptParsingException(script, e, lineNumber, line);
            }
        }
    }

    private Action parseBody(Script script) throws ScriptParsingException, ValidationIssue {
        Action action = new ScriptAction(n);
        previousActionNumber = action.number;

        try {
            StatementCollectors collectors = new StatementCollectors(script.collectorFactory);

            while ((line = readLine()) != null) {
                // Start of a new action
                if (line.startsWith(".")) {
                    parseStatement(script, action, collectors);
                } else if (line.toLowerCase().startsWith(ACTIONMATCH)) {
                    break;
                } else if (line.startsWith("[]")) {
                    if (action.interaction != null) {
                        if (!(action.interaction instanceof AbstractPause)) {
                            throw new ScriptParsingException(script, action,
                                    action.interaction.getClass().getSimpleName() + " is not allowed before anonymous actions", lineNumber, line);
                        }
                    }
                    for (StatementCollector statementCollector : collectors) {
                        statementCollector.nextSection(action);
                    }
                } else if (line.startsWith("[") && line.endsWith("]")) {
                    handleDeprecatedInlineReply();
                } else {
                    collectors.get(Statement.Message).parse(new ScriptLineTokenizer(Statement.Message, lineNumber, line, script, declarations));
                }
            }

            finalizeActionParsing(script, action, collectors);
        } catch (ScriptParsingException | ValidationIssue e) {
            throw e;
        } catch (Exception e) {
            throw new ScriptParsingException(script, action, e, lineNumber, line);
        }

        return action;
    }

    private static void handleDeprecatedInlineReply() {
        throw new IllegalArgumentException("Deprecated bracket prompt");
    }

    private void parseStatement(Script script, Action action, StatementCollectors collectors)
            throws ScriptParsingException, ValidationIssue, IOException {
        ScriptLineTokenizer cmd = lineNumber(script);
        if (collectors.canParse(cmd.statement)) {
            StatementCollector collector = collectors.get(cmd.statement);
            collector.parse(cmd);
        } else if (cmd.statement == Statement.Include) {
            parseSubscript(script, cmd);
        } else {
            action.add(cmd);
        }
    }

    private void parseSubscript(Script script, ScriptLineTokenizer cmd)
            throws IOException, ScriptParsingException, ValidationIssue {
        try (BufferedReader subScriptReader = scriptCache.subScript(cmd.allArgs());) {
            ScriptParser parser = new ScriptParser(subScriptReader, this);
            parser.parse(script);
            Action action;
            while ((action = parser.parseAction(script)) != null) {
                script.actions.put(action.number, action);
            }

            parser.parseAction(script);
        }
    }

    protected ScriptLineTokenizer lineNumber(Script script) {
        return new ScriptLineTokenizer(lineNumber, applyDefines(line), script, declarations);
    }

    private static void finalizeActionParsing(Script script, Action action, StatementCollectors collectors)
            throws ValidationIssue {
        if (collectors.hasParsed(Statement.Message) && collectors.hasParsed(Statement.Txt)) {
            throw new ValidationIssue(script, action,
                    "Spoken messages and .txt are exclusive because the TeaseLib PCMPlayer supports only one text area");
        }

        for (StatementCollector collector : collectors) {
            collector.applyTo(action);
        }

        action.finalizeParsing(script);
    }

    private String readLine() throws IOException {
        String readLine;
        parseScope: while ((readLine = reader.readLine()) != null) {
            lineNumber++;
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
