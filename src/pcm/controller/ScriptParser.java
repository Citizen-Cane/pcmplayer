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
import pcm.model.ScriptLineTokenizer;
import pcm.model.ScriptParsingException;
import pcm.model.StatementCollector;
import pcm.model.StatementCollectors;
import pcm.model.Symbols;
import pcm.model.ValidationIssue;

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
    private ScriptCache scriptCache;

    private final Map<String, String> defines;
    private final Declarations declarations;

    private String line = null;
    private int l = 0;
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
                    ScriptLineTokenizer cmd = new ScriptLineTokenizer(l, applyDefines(line), script, declarations);
                    if (cmd.statement == Statement.Define) {
                        defines.put(cmd.args()[0], cmd.argsFrom(1));
                    } else if (cmd.statement == Statement.Declare) {
                        declarations.add(cmd.args()[0], cmd.args()[1]);
                    } else if (cmd.statement == Statement.Include) {
                        parseSubscript(script, cmd);
                    } else {
                        script.add(cmd);
                    }
                } catch (IOException e) {
                    throw new ScriptParsingException(l, n, line, e.getClass().getName(), script);
                } catch (UnsupportedOperationException e) {
                    throw new ScriptParsingException(l, n, line, e.getMessage(), script);
                } catch (Throwable t) {
                    throw new ScriptParsingException(l, n, line, t, script);
                }
            } else {
                throw new ScriptParsingException(l, n, line, "Unexpected script input", script);
            }
        }
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

                    StatementCollectors collectors = new StatementCollectors(script.collectorFactory);

                    while ((line = readLine()) != null) {
                        // Start of a new action
                        if (line.startsWith(".")) {
                            parseStatement(script, action, collectors);
                        } else if (line.toLowerCase().startsWith(ACTIONMATCH)) {
                            break;
                        } else if (line.startsWith("[]")) {
                            for (StatementCollector statementCollector : collectors) {
                                statementCollector.nextSection(action);
                            }
                        } else if (line.startsWith("[") && line.endsWith("]")) {
                            handleDeprecatedInlineReply();
                        } else {
                            collectors.get(Statement.Message)
                                    .parse(new ScriptLineTokenizer(Statement.Message, l, line, script, declarations));
                        }
                    }
                    finalizeActionParsing(script, action, collectors);
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

    private static void handleDeprecatedInlineReply() {
        throw new IllegalArgumentException("Deprecated bracket prompt");
    }

    private void parseStatement(Script script, Action action, StatementCollectors collectors)
            throws ScriptParsingException, ValidationIssue, IOException {
        ScriptLineTokenizer cmd = getScriptLineTokenizer(script);
        if (collectors.canParse(cmd.statement)) {
            StatementCollector collector = collectors.get(cmd.statement);
            collector.parse(cmd);
        } else if (cmd.statement == Statement.Include) {
            parseSubscript(script, cmd);
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

    protected ScriptLineTokenizer getScriptLineTokenizer(Script script) {
        return new ScriptLineTokenizer(l, applyDefines(line), script, declarations);
    }

    private static void finalizeActionParsing(Script script, Action action, StatementCollectors collectors)
            throws ValidationIssue {
        if (collectors.hasParsed(Statement.Message) && collectors.hasParsed(Statement.Txt)) {
            throw new ValidationIssue(action,
                    "Spoken messages and .txt are exclusive because the TeaseLib PCMPlayer supports only one text area",
                    script);
        }

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
