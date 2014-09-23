package pcm.controller;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pcm.model.Action;
import pcm.model.ActionLoadSbd;
import pcm.model.ActionRange;
import pcm.model.ParseError;
import pcm.model.Script;
import pcm.model.ScriptError;
import pcm.model.ScriptExecutionError;
import pcm.model.ValidationError;
import pcm.state.Condition;
import pcm.state.State;
import pcm.state.Visual;
import teaselib.Host;
import teaselib.Persistence;
import teaselib.TeaseLib;
import teaselib.TeaseScript;

public class Player extends TeaseScript {

	private static final String SCRIPTS = "scripts/";

	private final ScriptCache scripts;

	public static boolean validateScripts = false;
	public static boolean debugOutput = false;

	Script script = null;
	ActionRange range = null;
	private final State state;
	boolean invokedOnAllSet;

	public Player(URI[] assets, String assetsBasePath, Host host,
			Persistence persistence) throws IOException {
		// TODO Singleton may be too inflexible
		// TODO Resource loader doesn't have to be replaceable
		super(new TeaseLib(host, persistence, assets, assetsBasePath));
		this.scripts = new ScriptCache(teaseLib.resources, SCRIPTS);
		this.state = new State(teaseLib.host, teaseLib.persistence);
		this.invokedOnAllSet = false;
	}

	public void play(String name) {
		try {
			script = scripts.get(name);
			if (validateScripts) {
				List<ValidationError> validationErrors = new ArrayList<>();
				validate(script, validationErrors);
				for (String s : scripts.names()) {
					Script other = scripts.get(s);
					if (other != script) {
						validate(other, validationErrors);
					}
				}
				if (validationErrors.size() > 0) {
					for (ScriptError scriptError : validationErrors) {
						TeaseLib.log(createErrorMessage(scriptError));
					}
					throw new ValidationError("Validation failed, "
							+ validationErrors.size() + " issues");
				}
			}
		} catch (ScriptError e) {
			showError(e);
			return;
		} catch (Throwable t) {
			showError(t, name);
			return;
		}
		if (script != null) {
			try {
				play();
			} catch (ScriptError e) {
				showError(e);
			} catch (Throwable e) {
				showError(e, name);
			}
		}
	}

	// Failure in script constructor
	private void showError(ScriptError e) {
		TeaseLib.log(this, e);
		showError(createErrorMessage(e));
	}

	private String createErrorMessage(ScriptError e) {
		Throwable cause = e.getCause();
		String scriptName = e.script != null ? e.script.name : script.name;
		final String message;
		if (cause != null) {
			message = "Script " + scriptName + ": " + e.getMessage() + "\n"
					+ cause.getClass().getSimpleName() + ": "
					+ cause.getMessage();
		} else {
			message = "Script " + scriptName + ": " + e.getMessage();
		}
		return message;
	}

	private void showError(Throwable t, String scriptName) {
		TeaseLib.log(this, t);
		Throwable cause = t.getCause();
		if (cause != null) {
			showError("Script " + scriptName + ", " + t.getMessage() + "\n"
					+ cause.getClass().getSimpleName() + ": "
					+ cause.getMessage());
		} else {
			showError("Script " + scriptName + ", " + t.getClass().getName()
					+ ": " + t.getMessage());
		}
	}

	private void showError(String error) {
		TeaseLib.log(error);
		teaseLib.host.show(error);
		List<String> choices = new ArrayList<>();
		choices.add("Oh Dear");
		choose(choices);
	}

	private void resetScript() throws ScriptExecutionError {
		TeaseLib.log("Starting script " + script.name);
		state.restore(script);
		invokedOnAllSet = false;
		script.execute(state);
		range = script.startRange;
		mistress = script.mistressImages;
		if (debugOutput) {
			// Inject start range for debug purposes
			// range = new ActionRange(1449); // Toys menu
			// range = new ActionRange(2692); // Punishment wank
			// range = new ActionRange(1011);
			// range = new ActionRange(1176);
			// range = new ActionRange(800); // Debug menu
			// range = new ActionRange(9500); // End with NoImage
			// range = new ActionRange(8090); // Wanking, sound and delay
			// range = new ActionRange(1004); // speech and comma
			// range = new ActionRange(1007); // Speech recognition, askYN
			// range = new ActionRange(1363); // Wait 10 seconds
			// range = new ActionRange(1038); // self-bondage warning (.txt)
			// range = new ActionRange(????); // Corner time .txt test
			// range = new ActionRange(3039); // Stop button test
			// range = new ActionRange(3078); // Test sequences after
			// implementing closure
			// range = new ActionRange(4849); // Test sequences after
			// implementing closure
		}
	}

	public void play() throws AllActionsSetException, ScriptExecutionError
			{
		resetScript();
		// TODO Search for any mistress instead of using hard-coded path
		Action action = null;
		try {
			while (range != null) {
				action = play(action, null);
			}
		} catch (ScriptError e) {
			throw e;
		} catch (Throwable t) {
			throw new ScriptExecutionError(action, "Error executing script", t,
					script);
		}
	}

	private Action play(Action action, ActionRange playRange)
			throws AllActionsSetException, ScriptExecutionError {
		while (true) {
			// Choose action
			action = getAction();
			if (playRange != null) {
				if (!playRange.contains(action.number)) {
					return action;
				}
			}
			// Process action
			range = execute(action);
			if (range == null) {
				// Quit
				action = null;
				show(null);
				break;
			} else if (range instanceof ActionLoadSbd) {
				ActionLoadSbd loadSbd = (ActionLoadSbd) range;
				script = loadSbd.script;
				resetScript();
				range = loadSbd;
				// Jumping into a different script definitely exits the play range
				action = getAction();
			}
			// TODO OnClose handler
		}
		return action;
	}

	private Action getAction() throws AllActionsSetException {
		Action action;
		List<Action> actions = range(script, range);
		action = chooseAction(actions);
		if (action == null) {
			TeaseLib.log("All actions set");
			if (script.onAllSet != null && invokedOnAllSet == false) {
				TeaseLib.log("Invoking OnAllSet handler");
				invokedOnAllSet = true;
				range = script.onAllSet;
				actions = range(script, range);
				if (actions.size() == 0) {
					throw new AllActionsSetException(action, script);
				} else {
					action = chooseAction(actions);
				}
			} else {
				throw new AllActionsSetException(action, script);
			}
		} else {
			action = chooseAction(actions);
		}
		return action;
	}

	public ActionRange execute(Action action) throws ScriptExecutionError {
		// Mark this action as executed
		state.set(action);
		// Perform commands
		action.execute(state);
		// Render visuals
		Runnable visuals = () -> {
			if (action.visuals != null) {
				for (Visual visual : action.visuals.values()) {
					visual.render(this);
				}
			}
		};
		ActionRange range = action.interaction.getRange(script, action,
				visuals, this);
		// May already have been called due to an implicit call to choice(),
		// but that isn't guaranteed
		completeAll();
		return range;
	}

	/**
	 * Get a list of actions that can be chosen according on the state
	 * 
	 * @param script
	 * @param range
	 * @param state
	 * @return
	 */
	public List<Action> range(Script script, ActionRange range) {
		List<Action> candidates = script.actions.getAll(range);
		if (range.start < range.end) {
			StringBuilder actions = null;
			for (Action action : candidates) {
				if (actions == null) {
					actions = new StringBuilder();
					actions.append(action.number);
				} else {
					actions.append(" ");
					actions.append(action.number);
				}
			}
			TeaseLib.log("-> Range " + range.start + "-" + range.end + ":");
		} else {
			TeaseLib.log("Action " + range.start);
		}
		List<Action> selectable = new LinkedList<Action>();
		List<Action> poss1 = null;
		List<Action> poss100 = null;
		for (Action action : candidates) {
			boolean getAction = !state.get(new Integer(action.number)).equals(
					State.SET);
			if (getAction) {
				if (action.conditions != null) {
					for (Condition condition : action.conditions) {
						if (condition.isTrueFor(state)) {
							continue;
						} else {
							TeaseLib.log("Action " + action.number + ": "
									+ condition.getClass().getSimpleName()
									+ condition.toString());
							getAction = false;
							break;
						}
					}
				}
				if (getAction) {
					// poss == 1 and poss == 100 are special cases
					if (action.poss != null) {
						if (action.poss == 1) {
							if (poss1 == null) {
								poss1 = new LinkedList<Action>();
							}
							poss1.add(action);
						} else if (action.poss == 100) {
							if (poss100 == null) {
								poss100 = new LinkedList<Action>();
							}
							poss100.add(action);
						} else {
							selectable.add(action);
						}
					} else {
						selectable.add(action);
					}
				}
			}
		}
		if (poss1 == null && poss100 == null) {
			return selectable;
		} else if (poss100 != null) {
			// poss == 100 overrides
			return poss100;
		} else if (poss1 != null && selectable.size() == 0) {
			// poss == 1 is last chance
			return poss1;
		} else {
			return selectable;
		}
	}

	/**
	 * Choose an action from the list based on probability weights
	 * 
	 * @param actions
	 *            A list of executable actions, sorted out by getActions(...)
	 * @return
	 */
	public Action chooseAction(List<Action> actions) {
		if (actions.size() == 0) {
			return null;
		} else if (actions.size() == 1) {
			return actions.get(0);
		} else {
			StringBuilder actionList = null;
			for (Action action : actions) {
				int number = action.number;
				if (actionList == null) {
					actionList = new StringBuilder("Action:\t" + number);
				} else {
					actionList.append("\t");
					actionList.append(number);
				}
			}
			TeaseLib.log(actionList.toString());
			// Normalize all actions into the interval [0...100], the choose one
			// "poss" 100 is used to implement an "else" clause, since PCM
			// script
			// doesn't have one otherwise
			double normalized = 100.0;
			double weights[] = new double[actions.size()];
			double sum = 0.0;
			StringBuilder weightList = null;
			for (int i = 0; i < weights.length; i++) {
				Action action = actions.get(i);
				Integer weight = action.poss;
				double relativeWeight = normalized / weights.length;
				sum += weight != null ? weight
				// This would be mathematically correct
				// if none of the action specified a "poss" value
						: relativeWeight;
				weights[i] = sum;
				String w = String.format("%.2f", relativeWeight);
				if (weightList == null) {
					weightList = new StringBuilder("Weight:\t" + w);
				} else {
					weightList.append("\t");
					weightList.append(w);
				}
			}
			TeaseLib.log(weightList.toString());
			// Normalize and build interval
			for (int i = 0; i < weights.length; i++) {
				weights[i] *= normalized / sum;
			}
			// Choose a value
			double value = getRandom(0, (int) normalized);
			Action action = null;
			for (int i = 0; i < weights.length; i++) {
				if (value <= weights[i]) {
					action = actions.get(i);
					break;
				}
			}
			if (action == null) {
				action = actions.get(weights.length);
			}
			TeaseLib.log("Random = " + value + " -> choosing action "
					+ action.number);
			return action;
		}
	}

	public void validate(Script script, List<ValidationError> validationErrors)
			throws ParseError {
		script.validate(validationErrors);
		for (Action action : script.actions.values()) {
			// if (action.image != null && !action.image.isEmpty())
			// {
			// try {
			// String path = IMAGES + action.image;
			// resources.image(path);
			// } catch (IOException e) {
			// throw new ValidationError("", e);
			// }
			// }
			action.validate(script, validationErrors);
			for (ScriptError scriptError : validationErrors) {
				if (scriptError.script == null) {
					scriptError.script = script;
				}
			}
		}
	}
}

// http://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file
// http://www.uofr.net/~greg/java/get-resource-listing.html
