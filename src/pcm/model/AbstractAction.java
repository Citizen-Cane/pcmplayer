package pcm.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import pcm.state.Command;
import pcm.state.State;
import pcm.state.commands.ResetRange;
import teaselib.TeaseLib;

public abstract class AbstractAction {

	/// Names of Statements used in PCM sbd scripts 
	public enum Statement
	{
		// Conditions
		Must,
		MustNot,
		Poss,
		TimeFrom,
		NumActionsFrom,
		
		// Commands
		Set,
		UnSet,
		SetTime,
		ResetRange,
		SetRange,
		Repeat,
		RepeatAdd,
		RepeatDel,
		Save,

		// Next action
		Range,
		LoadSbd,
		Quit,
		
		// Script-global
		Debug,
		Restore,
		BackColor,
		TextColor,
		OnAllSet,
		OnClose,
		SsDir,
		
		// Script and action
		YesText,
		NoText,
		ResumeText,
		
		// Visual
		NoImage,
		Image,
		Message,
		Txt,
		PlayWav,
		PlayAvi,
		Say,
		Exec,
		
		// Interaction
		YesNo,
		Pause,
		PopUp,
		Ask,
		
		// Menus
		AskTitle,
		AskCheck,
		Menu,

		// Obsolete
		Delay,       // delay is based on text length,
		ActionDelay, // ActionDelay is used to specify absolute delay
		// TODO Remove delay 0, delay x from all over the script
		// Only keep delay xy STOP n, and explicit ActionDelay statements
		// TODO Also, .Delay 0 implies .NoImage -> can be removed in script,
		// but the .NoImage statement is needed in some situations
		
		// Add-Ons
		StopText,
		Inject,
		;
		public final static Map<String, Statement> lookup = new HashMap<>();
	}

	{
		for(Statement name : EnumSet.allOf(Statement.class))
		{
			Statement.lookup.put(name.toString().toLowerCase(), name);
		}
	}

	final static String yesTextDefault = "Yes";
	final static String noTextDefault = "No";
	final static String resumeTextDefault = "Resume";
	final static String stopTextDefault = "Stop";

	public String yesText = null;
	public String noText = null;
	public String resumeText = null;
	public String stopText = null;

	public Vector<Command> commands = null;
	
	public void addCommand(Command command)
	{
		if (this.commands == null)
		{
			this.commands = new Vector<>();
		}
		commands.add(command);
	}

	public ActionRange execute(State state) throws ScriptExecutionError
	{
		if (commands != null)
		{
			for(Command command : commands)
			{
				TeaseLib.log(command.getClass().getSimpleName() + " " + command.toString());
				command.execute(state);
			}
		}
		return null;
	}

	public void add(ScriptLineTokenizer cmd)
	{
		Statement name = cmd.statement;
		if (name == Statement.YesText)
		{
			yesText = allArgsFrom(cmd.args());
		}
		else if (name == Statement.NoText)
		{
			noText = allArgsFrom(cmd.args());
		}
		else if (name == Statement.ResumeText)
		{
			resumeText = allArgsFrom(cmd.args());
		}
		else if (name == Statement.StopText)
		{
			stopText = allArgsFrom(cmd.args());
		}
		else if (name == Statement.ResetRange)
		{
			String args[] = cmd.args();
			addCommand(new ResetRange(
					Integer.parseInt(args[0]),
					Integer.parseInt(args[1])));
		}
		else
		{
			throw new UnsupportedOperationException("Statement ." + name.toString() + " not implemented");
		}
	}
	
	public String allArgsFrom(String[] args)
	{
		return allArgsFrom(args, 0);
	}

	public String allArgsFrom(String[] args, int n)
	{
		StringBuilder s = new StringBuilder();
		s.append(args[n]);
		for(int i = n + 1; i < args.length; i++)
		{
			s.append(" ");
			s.append(args[i]);
		}
		return s.toString();
	}
}
