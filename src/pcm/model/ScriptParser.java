package pcm.model;

import java.io.BufferedReader;
import java.io.IOException;

import pcm.model.AbstractAction.Statement;
import pcm.state.visuals.SpokenMessage;
import pcm.state.visuals.Txt;

public class ScriptParser {

    private final BufferedReader reader;
    private String line = null;
    private int l = 0;
    private int n = 0;
    private int previousActionNumber = 0;

    private Script script;

    private final static String ACTIONMATCH = "[action ";
    private final static String COMMENT = "'";

    public ScriptParser(Script script, BufferedReader reader) {
        this.script = script;
        this.reader = reader;
    }

    public void parseScript() throws ParseError, IOException {
        while ((line = readLine()) != null) {
            if (line.toLowerCase().startsWith(ACTIONMATCH)) {
                return;
            } else if (line.startsWith(".")) {
                try {
                    ScriptLineTokenizer cmd = new ScriptLineTokenizer(l, line);
                    script.add(cmd);
                } catch (UnsupportedOperationException e) {
                    throw new ParseError(l, n, line, e.getMessage(), script);
                } catch (Throwable t) {
                    throw new ParseError(l, n, line, t, script);
                }
            } else {
                throw new ParseError(l, n, line, "Unexpected script input",
                        script);
            }
        }
        return;
    }

    public Action parseAction() throws ParseError, ValidationError, IOException {
        if (line == null) {
            return null;
        } else {
            Action action = null;
            try {
                // Start new action
                if (n > 0) {
                    n = 0;
                    action = null;
                }
                int start = ACTIONMATCH.length();
                int end = line.indexOf("]");
                if (end < start) {
                    throw new ParseError(l, 0, line, "Invalid action number");
                }
                n = Integer.parseInt(line.substring(start, end));
                if (n <= previousActionNumber) {
                    throw new ParseError(l, n, line,
                            "Action must be defined in increasing order");
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
                            message.newSection();
                        }
                        // Other statements
                        else if (line.startsWith(".")) {
                            // .txt messages must be executed last,
                            // so this has to be added last
                            ScriptLineTokenizer cmd = new ScriptLineTokenizer(
                                    l, line);
                            if (cmd.statement == Statement.Txt) {
                                String text = line.substring(Statement.Txt
                                        .toString().length() + 1);
                                // Trim one leading space
                                if (!text.isEmpty()) {
                                    text = text.substring(1);
                                }
                                if (txt == null) {
                                    txt = new Txt(text);
                                } else {
                                    txt.add(text);
                                }
                            } else {
                                action.add(cmd);
                            }
                        }
                        // spoken Message
                        else {
                            if (message == null) {
                                message = new SpokenMessage(line);
                            } else {
                                message.add(line);
                            }
                        }
                    }
                    // Add message to visuals as the last item, because
                    // rendering the message triggers rendering of all other
                    // visuals
                    if (message != null) {
                        action.addVisual(Statement.Message, message);
                    }
                    if (txt != null) {
                        action.addVisual(Statement.Txt, txt);
                    }
                    action.finalizeParsing();
                }
            } catch (ParseError e) {
                if (e.script == null) {
                    e.script = script;
                }
                throw e;
            } catch (ValidationError e) {
                if (e.script == null) {
                    e.script = script;
                }
                throw e;
            } catch (Throwable t) {
                // TODO Collect these in list
                throw new ParseError(l, n, line, t, script);
            }
            return action;
        }
    }

    private String readLine() throws IOException {
        String readLine;
        while ((readLine = reader.readLine()) != null) {
            l++;
            readLine = readLine.trim();
            if (readLine.isEmpty() || readLine.startsWith(COMMENT)) {
                continue;
            } else {
                break;
            }
        }
        return readLine;
    }
}
