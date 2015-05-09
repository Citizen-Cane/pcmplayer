package pcm.controller;

import java.io.IOException;
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
import pcm.state.MappedState;
import pcm.state.State;
import pcm.state.Validatable;
import pcm.state.Visual;
import teaselib.Actor;
import teaselib.DummyHost;
import teaselib.DummyPersistence;
import teaselib.ScriptInterruptedException;
import teaselib.TeaseLib;
import teaselib.TeaseScript;
import teaselib.persistence.Toys;
import teaselib.texttospeech.ScriptScanner;
import teaselib.texttospeech.TextToSpeechRecorder;

public abstract class Player extends TeaseScript {

    private static final String SCRIPTS = "scripts/";

    private final ScriptCache scripts;

    public static boolean validateScripts = false;
    public static boolean debugOutput = false;

    Script script = null;
    public ActionRange range = null;
    private final MappedState state;
    boolean invokedOnAllSet;

    public static void main(String argv[]) {
        String resourcesBase = "scripts/";
        String assetRoot = "Mine/";
        try {
            // TODO Init resource in teaselib init, after initializing the logs
            // TODO Test whether all URI exist, without the IOException would be
            // inappropriate
            TeaseLib teaseLib = new TeaseLib(new DummyHost(),
                    new DummyPersistence(), resourcesBase, assetRoot);
            teaseLib.addAssets("Mine Scripts.zip", "Mine Resources.zip",
                    "Mine Mistress.zip");
            ScriptCache scripts = new ScriptCache(teaseLib.resources, SCRIPTS);
            // Get the main script
            Script main = scripts.get("Mine");
            // and validate to load all the sub scripts
            validate(main, new ArrayList<ValidationError>());
            TextToSpeechRecorder recorder = new TextToSpeechRecorder(
                    teaseLib.resources);
            Actor actor = new Actor(Actor.Dominant, "en-us");
            for (String scriptName : scripts.names()) {
                Script script = scripts.get(scriptName);
                ScriptScanner scriptScanner = new PCMScriptScanner(script,
                        actor);
                recorder.create(scriptScanner);
            }
            recorder.finish();
        } catch (ParseError e) {
            TeaseLib.log(argv, e);
        } catch (ValidationError e) {
            TeaseLib.log(argv, e);
        } catch (IOException e) {
            TeaseLib.log(argv, e);
        } catch (Throwable t) {
            TeaseLib.log(argv, t);
        }
        System.exit(0);
    }

    public Player(TeaseLib teaseLib, String locale) throws IOException {
        super(teaseLib, locale);
        this.scripts = new ScriptCache(teaseLib.resources, SCRIPTS);
        this.invokedOnAllSet = false;
        MappedState mappedState = new MappedState(this,
                teaseLib.resources.namespace, teaseLib.host,
                teaseLib.persistence);
        this.state = mappedState;
        // Test code for mappings, should end up in script
        // Toy categories - multiple items on host
        mappedState.addMapping(311, get(Toys.Wrist_Restraints));
        mappedState.addMapping(312, get(Toys.Ankle_Restraints));
        mappedState.addMapping(325, get(Toys.Collars));
        mappedState.addMapping(340, get(Toys.Gags));
        mappedState.addMapping(350, get(Toys.Buttplugs));
        mappedState.addMapping(370, get(Toys.Spanking_Implements));
        mappedState.addMapping(380, get(Toys.Chastity_Devices));
        mappedState.addMapping(389, get(Toys.Vibrators));

        // Toy simple mappings
        mappedState.addMapping(301, get(Toys.Nipple_clamps));
        mappedState.addMapping(310, get(Toys.Clothespins));
        mappedState.addMapping(330, get(Toys.Rope));
        mappedState.addMapping(334, get(Toys.Chains));
        mappedState.addMapping(382, get(Toys.Blindfold));
        mappedState.addMapping(384, get(Toys.Humbler));
        mappedState.addMapping(388, get(Toys.Anal_Dildo));

        mappedState.addMapping(383, get(Toys.Enema_Kit));
        // TODO mappedState.addMapping(383, get(Toys.Enema_Bulb));
        mappedState.addMapping(387, get(Toys.Pussy_Clamps));
        mappedState.addMapping(385, get(Toys.Ball_Stretcher));
    }

    public void play(String name, ActionRange startRange) {
        try {
            script = scripts.get(name);
            if (validateScripts) {
                validateAll();
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
                resetScript();
                if (startRange != null) {
                    range = startRange;
                } else {
                    range = script.startRange;
                }
                while (range != null) {
                    play((ActionRange) null);
                }
            } catch (ScriptError e) {
                showError(e);
            } catch (Throwable e) {
                showError(e, name);
            }
        }
    }

    private void validateAll() throws ParseError, ValidationError, IOException {
        List<ValidationError> validationErrors = new ArrayList<ValidationError>();
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
                    + validationErrors.size() + " issues", script);
        }
    }

    private void resetScript() throws ScriptExecutionError {
        TeaseLib.log("Starting script " + script.name);
        state.restore(script);
        invokedOnAllSet = false;
        script.execute(state);
        // TODO Search for any mistress instead of using hard-coded path
        dominantImages = script.mistressImages;
    }

    public void play(ActionRange playRange) throws AllActionsSetException,
            ScriptExecutionError {
        while (true) {
            // Choose action
            Action action = getAction();
            try {
                if (playRange != null) {
                    if (!playRange.contains(action.number)) {
                        range = new ActionRange(action.number);
                        return;
                    }
                }
                // Process action
                range = execute(action);
                if (range == null) {
                    // Quit
                    action = null;
                    showImage(NoImage);
                    show(null);
                    break;
                } else if (range instanceof ActionLoadSbd) {
                    ActionLoadSbd loadSbd = (ActionLoadSbd) range;
                    script = loadSbd.script;
                    resetScript();
                    range = loadSbd;
                    // Jumping into a different script definitely exits the play
                    // range
                    action = getAction();
                }
            } catch (ScriptInterruptedException e) {
                throw e;
            } catch (ScriptExecutionError e) {
                throw e;
            }
            // TODO OnClose handler
            catch (Throwable t) {
                throw new ScriptExecutionError(action,
                        "Error executing script", t, script);
            }
        }
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
        }
        return action;
    }

    public ActionRange execute(final Action action) throws ScriptExecutionError {
        // Mark this action as executed
        state.set(action);
        // Perform commands
        action.execute(state);
        // Render visuals
        // It looks so much better with 1.8 ...
        /*
         * Runnable visuals = () -> { if (action.visuals != null) { for (Visual
         * visual : action.visuals.values()) { visual.render(this); } } };
         */

        // One would think that we have to wait for all visuals to
        // at
        // least complete
        // their mandatory part. But interactions perform
        // differently,
        // for instance
        // class Ask displays its user interface while the visuals
        // render, to allow
        // the message to be spoken during checkbox selection.
        // Interactions that eventually call choose(...) do this
        // implicitly, but all other classes like Range have to call
        // it
        // when suitable,
        // to prevent text and messages appearing too fast
        Runnable visuals = new Runnable() {
            @Override
            public void run() {
                if (action.visuals != null) {
                    for (Visual visual : action.visuals.values()) {
                        render(visual);
                    }
                }
            }
        };
        // May already have been called due to an implicit call to choice(),
        // but that isn't guaranteed
        completeAll();
        ActionRange range = action.interaction.getRange(script, action,
                visuals, this);
        return range;
    }

    public void render(Visual visual) {
        visual.render(this);
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
        Action action;
        if (actions.size() == 0) {
            action = null;
        } else if (actions.size() == 1) {
            action = actions.get(0);
            TeaseLib.log("-> choosing action " + action.number);
        } else {
            StringBuilder actionList = null;
            for (Action a : actions) {
                int number = a.number;
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
                Action a = actions.get(i);
                Integer weight = a.poss;
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
            action = null;
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
        }
        return action;
    }

    public static void validate(Script script,
            List<ValidationError> validationErrors) throws ParseError {
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
            if (action.visuals != null) {
                for (Visual visual : action.visuals.values()) {
                    if (visual instanceof Validatable) {
                        ((Validatable) visual).validate(script, action,
                                validationErrors);
                    }
                }
            }
            for (ScriptError scriptError : validationErrors) {
                if (scriptError.script == null) {
                    scriptError.script = script;
                }
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
        teaseLib.host.show(null, error);
        List<String> choices = new ArrayList<String>();
        choices.add("Oh Dear");
        choose(choices);
    }
}

// http://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file
// http://www.uofr.net/~greg/java/get-resource-listing.html
