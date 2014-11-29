package pcm.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import pcm.model.AbstractAction.Statement;
import pcm.state.visuals.Message;

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
					parseStatement(script, line);
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
					Vector<String> message = null;
					while ((line = readLine()) != null) {
						if (line.toLowerCase().startsWith(ACTIONMATCH)) {
							break;
						} else if (line.startsWith(".")) {
							parseStatement(action, line);
						} else {
							if (message == null) {
								message = new Vector<>();
							}
							message.add(line);
						}
					}
					if (message != null) {
						action.addVisual(Statement.Message,
								new Message(message));
					}
					action.finalizeParsing();
				}
			} catch (ScriptError e) {
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

	private void parseStatement(AbstractAction abstractAction, String line)
			throws IllegalArgumentException, ParseError {

		ScriptLineTokenizer cmd = new ScriptLineTokenizer(l, line);
		// Global statements
		if (cmd.statement == Statement.Inject) {
			throw new UnsupportedOperationException(
					"Injections may lead to script inconsistencies");
			// Inject command
			// String[] args = cmd.args();
			// Statement injectedStatement =
			// parseStatement(args[2].substring(1));
			// String[] injection = new String[args.length - 3];
			// System.arraycopy(args, 3, injection, 0, injection.length);
			// abstractAction.addCommand(new Injection(script, Integer
			// .parseInt(args[0]), Integer.parseInt(args[1]),
			// injectedStatement, injection));
		} else {
			// Local statements
			abstractAction.add(cmd);
		}
	}

	// private Statement parseStatement(String statement) {
	// String key = statement.toLowerCase();
	// if (Statement.lookup.containsKey(key)) {
	// return Statement.lookup.get(key);
	// } else {
	// throw new IllegalArgumentException("Unknown statement " + statement);
	// }
	// }
}
