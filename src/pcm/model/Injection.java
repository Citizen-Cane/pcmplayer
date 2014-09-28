package pcm.model;

import pcm.model.AbstractAction.Statement;
import pcm.state.Command;
import pcm.state.State;

@Deprecated
public class Injection implements Command {
	public final int start;
	public final int end;
	public final Statement statement;
	public final String[] args;

	private final Script script;
	private boolean applied = false;

	public Injection(Script script, int start, int end, Statement statement,
			String[] args) {
		this.script = script;
		this.start = start;
		this.end = end;
		this.statement = statement;
		this.args = args;
	}

	@Override
	public void execute(State state) throws ScriptExecutionError {
		if (!applied) {
			// Build cmd line
			StringBuilder line = new StringBuilder();
			// Statement
			line.append(".");
			line.append(statement.toString());
			// Args
			for(String arg : args)
			{
				line.append(" ");
				line.append(arg);
			}
			ScriptLineTokenizer cmd = new ScriptLineTokenizer(0, line.toString());
			// Inject
			for (Action action : script.actions.getAll(new ActionRange(start,
					end))) {
				try {
					action.add(cmd);
				} catch (ParseError e) {
					throw new ScriptExecutionError("Error parsing injecte statement", e);
				}
			}
			applied = true;
		}
	}

	@Override
	public String toString() {
		StringBuilder allArgs = new StringBuilder();
		for (String arg : args) {
			if (allArgs.length() == 0) {
				allArgs.append(arg);
			} else {
				allArgs.append(" ");
				allArgs.append(arg);
			}
		}
		return start + "-" + end + "." + statement.toString()
				+ allArgs.toString();
	}
}
