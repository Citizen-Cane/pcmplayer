package pcm.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Stack;

import pcm.model.AbstractAction.Statement;
import pcm.model.Action;
import pcm.model.Script;
import pcm.model.ScriptLineTokenizer;
import pcm.model.ScriptParsingException;
import pcm.model.Symbols;
import pcm.model.ValidationIssue;
import pcm.state.visuals.SpokenMessage;
import pcm.state.visuals.Txt;

public class ScriptParser {
    private final static String ACTIONMATCH = "[action ";
    private final static String COMMENT = "'";
    private static final String DEFINE_IF = "#if";
    private static final String DEFINE_ELSE = "#else";
    private static final String DEFINE_ENDIF = "#endif";

    private final Symbols staticSymbols;
    private final Stack<String[]> preprocessorScope = new Stack<String[]>();
    private final BufferedReader reader;
    private final String resourcePath;

    private String line = null;
    private int l = 0;
    private int n = 0;
    private int previousActionNumber = 0;

    public ScriptParser(BufferedReader reader, String resourcePath,
            Symbols staticSymbols) {
        this.reader = reader;
        this.resourcePath = resourcePath;
        this.staticSymbols = staticSymbols;
    }

    public void parse(Script script)
            throws ScriptParsingException, IOException {
        while ((line = readLine()) != null) {
            if (line.toLowerCase().startsWith(ACTIONMATCH)) {
                return;
            } else if (line.startsWith(".")) {
                try {
                    ScriptLineTokenizer cmd = new ScriptLineTokenizer(l, line);
                    script.add(cmd);
                } catch (UnsupportedOperationException e) {
                    throw new ScriptParsingException(l, n, line, e.getMessage(),
                            script);
                } catch (Throwable t) {
                    throw new ScriptParsingException(l, n, line, t, script);
                }
            } else {
                throw new ScriptParsingException(l, n, line,
                        "Unexpected script input", script);
            }
        }
        return;
    }

    public Action parseAction(Script script)
            throws ScriptParsingException, ValidationIssue {
        if (line == null) {
            return null;
        } else {
            Action action = null;
            try {
                // Start new action
                if (n > 0) {
                    n = 0;
                }
                int start = ACTIONMATCH.length();
                int end = line.indexOf("]");
                if (end < start) {
                    throw new ScriptParsingException(l, 0, line,
                            "Invalid action number", script);
                }
                n = Integer.parseInt(line.substring(start, end));
                if (n <= previousActionNumber) {
                    throw new ScriptParsingException(l, n, line,
                            "Action must be defined in increasing order",
                            script);
                } else {
                    action = new Action(n);
                    previousActionNumber = action.number;
                    SpokenMessage message = null;
                    Txt txt = null;
                    while ((line = readLine()) != null) {
                        // Start of a new action
                        if (line.toLowerCase().startsWith(ACTIONMATCH)) {
                            break;
                        }
                        // another message
                        else if (line.startsWith("[]")) {
                            if (message != null) {
                                message.completeSection();
                                message.startNewSection();
                            }
                        }
                        // inline reply
                        else if (line.startsWith("[") && line.endsWith("]")) {
                            if (message != null) {
                                message.completeSection(
                                        line.substring(1, line.length() - 1));
                                message.startNewSection();
                            }
                        }
                        // Other statements
                        else if (line.startsWith(".")) {
                            // .txt messages must be executed last,
                            // so this has to be added last
                            ScriptLineTokenizer cmd = new ScriptLineTokenizer(l,
                                    line);
                            if (cmd.statement == Statement.Txt) {
                                String text = line.substring(
                                        Statement.Txt.toString().length() + 1);
                                // Trim one leading space
                                if (!text.isEmpty()) {
                                    text = text.substring(1);
                                }
                                if (txt == null) {
                                    txt = new Txt(script.actor);
                                }
                                txt.add(text);
                            } else {
                                action.add(cmd);
                            }
                        }
                        // spoken Message
                        else {
                            if (message == null) {
                                message = new SpokenMessage(script.actor);
                            }
                            message.add(line, resourcePath);
                        }
                    }
                    // Add message to visuals as the last item, because
                    // rendering the message triggers rendering of all other
                    // visuals
                    if (message != null) {
                        message.completeMessage();
                        action.addVisual(Statement.Message, message);
                    }
                    if (txt != null) {
                        txt.end();
                        action.addVisual(Statement.Txt, txt);
                    }
                    action.finalizeParsing(script);
                }
            } catch (ScriptParsingException e) {
                if (e.script == null) {
                    e.script = script;
                }
                throw e;
            } catch (ValidationIssue e) {
                if (e.script == null) {
                    e.script = script;
                }
                throw e;
            } catch (Throwable t) {
                // TODO Collect these in list
                throw new ScriptParsingException(l, n, line, t, script);
            }
            return action;
        }
    }

    private String readLine() throws IOException {
        String readLine;
        readline: while ((readLine = reader.readLine()) != null) {
            l++;
            readLine = readLine.trim();
            if (readLine.isEmpty() || readLine.startsWith(COMMENT)) {
                continue;
            } else if (readLine.startsWith("#")) {
                String[] args = readLine.replace("\t", " ").split(" ");
                String command = args[0].toLowerCase();
                if (DEFINE_IF.equals(command)) {
                    preprocessorScope.push(args);
                    for (int i = 1; i < args.length; i++) {
                        if (staticSymbols.containsKey(args[i])) {
                            continue readline;
                        }
                    }
                    readLine = consumeScope();
                    args = readLine.replace("\t", " ").split(" ");
                    command = args[0].toLowerCase();
                    if (DEFINE_ELSE.equals(command)) {
                        continue;
                    } else if (DEFINE_ENDIF.equals(command)) {
                        preprocessorScope.pop();
                    }
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

    private String consumeScope() throws IOException {
        String readLine;
        int n = 1;
        while ((readLine = reader.readLine()) != null && n > 0) {
            String[] args = readLine.replace("\t", " ").split(" ");
            String command = args[0].toLowerCase();
            if (DEFINE_IF.equalsIgnoreCase(command)) {
                n++;
            } else if (DEFINE_ENDIF.equalsIgnoreCase(command)) {
                if (--n == 0) {
                    break;
                }
            } else if (DEFINE_ELSE.equalsIgnoreCase(command)) {
                if (--n == 0) {
                    break;
                }
            }
        }
        return readLine;
    }
}
