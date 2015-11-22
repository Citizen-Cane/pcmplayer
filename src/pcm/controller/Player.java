package pcm.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import pcm.model.AbstractAction;
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
import teaselib.Message;
import teaselib.ScriptFunction;
import teaselib.TeaseLib;
import teaselib.TeaseScript;
import teaselib.Toys;
import teaselib.core.ResourceLoader;
import teaselib.core.ScriptInterruptedException;
import teaselib.core.speechrecognition.SpeechRecognitionResult.Confidence;
import teaselib.core.texttospeech.ScriptScanner;
import teaselib.core.texttospeech.TextToSpeechRecorder;
import teaselib.hosts.DummyHost;
import teaselib.hosts.DummyPersistence;
import teaselib.util.RandomImages;
import teaselib.util.SpeechRecognitionRejectedScript;

public abstract class Player extends TeaseScript {

    public static final String Scripts = "scripts/";

    private final ScriptCache scripts;
    private final MappedState state;
    private final String mistressPath;

    public static boolean validateScripts = false;
    public static boolean debugOutput = false;

    Script script = null;
    public ActionRange range = null;
    boolean invokedOnAllSet;
    boolean intentionalQuit = false;

    /**
     * This range can be pushed onto the script range stack to tell the player
     * to hand over execution from the PCM script engine back to the player.
     * 
     * Push it on the stack, and call {@link Player#play(ActionRange)}. The
     * current script is executed up to the next occurrence of a
     * {@link AbstractAction.Statement#Return} statement.
     */
    public final static ActionRange ReturnToPlayer = new ActionRange(0);

    public static void recordVoices(String basePath, String assetRoot,
            Actor actor, String[] assets, String startupScript)
            throws IOException, ValidationError, ParseError {
        TeaseLib.init(new DummyHost(), new DummyPersistence());
        ResourceLoader resources = new ResourceLoader(basePath, assetRoot);
        resources.addAssets(assets);
        ScriptCache scripts = new ScriptCache(resources, Scripts);
        // Get the main script
        TextToSpeechRecorder recorder = new TextToSpeechRecorder(resources);
        Script main = scripts.get(actor, startupScript);
        // and validate to load all the sub scripts
        validate(main, new ArrayList<ValidationError>());
        for (String scriptName : scripts.names()) {
            Script script = scripts.get(actor, scriptName);
            ScriptScanner scriptScanner = new PCMScriptScanner(script);
            recorder.create(scriptScanner);
        }
        recorder.finish();
    }

    public Player(TeaseLib teaseLib, ResourceLoader resources, Actor actor,
            String namespace, String mistressPath) {
        super(teaseLib, resources, actor, namespace);
        this.scripts = new ScriptCache(resources, Scripts);
        this.mistressPath = mistressPath;
        this.invokedOnAllSet = false;
        MappedState mappedState = new MappedState(this);
        this.state = mappedState;
        // Test code for mappings, should end up in script
        // Toy categories - multiple items on host
        mappedState.addMapping(311, toys(Toys.Wrist_Restraints));
        mappedState.addMapping(312, toys(Toys.Ankle_Restraints));
        mappedState.addMapping(325, toys(Toys.Collars));
        mappedState.addMapping(340, toys(Toys.Gags));
        mappedState.addMapping(350, toys(Toys.Buttplugs));
        mappedState.addMapping(370, toys(Toys.Spanking_Implements));
        mappedState.addMapping(380, toys(Toys.Chastity_Devices));
        mappedState.addMapping(389, toys(Toys.Vibrators, Toys.EStim_Devices));

        // Toy simple mappings
        mappedState.addMapping(301, toy(Toys.Nipple_clamps));
        mappedState.addMapping(310, toy(Toys.Clothespins));
        mappedState.addMapping(330, toy(Toys.Rope));
        mappedState.addMapping(334, toy(Toys.Chains));
        mappedState.addMapping(382, toy(Toys.Blindfold));
        mappedState.addMapping(384, toy(Toys.Humbler));
        mappedState.addMapping(388, toy(Toys.Anal_Dildo));

        mappedState.addMapping(383, toy(Toys.Enema_Kit));
        // TODO mappedState.addMapping(383, get(Toys.Enema_Bulb));
        mappedState.addMapping(387, toy(Toys.Pussy_Clamps));
        mappedState.addMapping(385, toy(Toys.Ball_Stretcher));
    }

    public void play(String name, ActionRange startRange) {
        SpeechRecognitionRejectedScript srRejectedHandler = actor.speechRecognitionRejectedHandler;
        actor.speechRecognitionRejectedHandler = new SpeechRecognitionRejectedScript(
                this) {
            @Override
            public boolean canRun() {
                return Player.this.script.onRecognitionRejected != null
                        && !intentionalQuit;
            }

            @Override
            public void run() {
                try {
                    // The play(Range) method must end on return, not continue
                    // somewhere else
                    Player.this.scripts.stack.push(ReturnToPlayer);
                    range = Player.this.script.onRecognitionRejected;
                    Player.this.play(new ActionRange(0, Integer.MAX_VALUE));
                    if (range == null) {
                        // Intentional quit
                        intentionalQuit = true;
                        throw new ScriptInterruptedException();
                        // TODO Interrupting the main script results in the
                        // onClose handler to be triggered -> bad quit
                    }
                } catch (AllActionsSetException e) {
                    teaseLib.log.error(this, e);
                } catch (ScriptExecutionError e) {
                    teaseLib.log.error(this, e);
                }
            }
        };
        try {
            script = scripts.get(actor, name);
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
            } catch (ScriptInterruptedException e) {
                if (!intentionalQuit) {
                    teaseLib.log.error(this, e);
                }
            } catch (ScriptError e) {
                showError(e);
            } catch (Throwable e) {
                showError(e, name);
            } finally {
                teaseLib.host.setQuitHandler(null);
                intentionalQuit = false;
                actor.speechRecognitionRejectedHandler = srRejectedHandler;
            }
        }
    }

    private void validateAll() throws ParseError, ValidationError, IOException {
        List<ValidationError> validationErrors = new ArrayList<ValidationError>();
        validate(script, validationErrors);
        for (String s : scripts.names()) {
            Script other = scripts.get(actor, s);
            if (other != script) {
                validate(other, validationErrors);
            }
        }
        if (validationErrors.size() > 0) {
            for (ScriptError scriptError : validationErrors) {
                teaseLib.log.info(createErrorMessage(scriptError));
            }
            throw new ValidationError("Validation failed, "
                    + validationErrors.size() + " issues", script);
        }
    }

    private void resetScript() throws ScriptExecutionError {
        teaseLib.log.info("Starting script " + script.name);
        state.restore(script);
        invokedOnAllSet = false;
        if (script.onClose != null) {
            final Thread scriptThread = Thread.currentThread();
            teaseLib.host.setQuitHandler(new TeaseScript(this) {
                @Override
                public void run() {
                    teaseLib.log.info("Interrupting script thread '"
                            + scriptThread.getName() + "'");
                    scriptThread.interrupt();
                    // The main script continues at the onClose range
                }
            });
        }
        script.execute(state);
        // TODO Search for any mistress instead of using hard-coded path
        actor.images = new RandomImages(resources, mistressPath
                + script.mistressImages);
    }

    /**
     * Plays the given range. Returns on {@code .quit}, if
     * {@link Player#ReturnToPlayer} had been pushed on the stack and a return
     * statement is executed, or if the the next action is not a member of the
     * play range.
     * 
     * @param playRange
     *            The range to play.
     * @throws AllActionsSetException
     * @throws ScriptExecutionError
     */
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
                range = execute(action);
                if (range == null) {
                    // Quit
                    setImage(Message.NoImage);
                    show("");
                    break;
                } else if (range == ReturnToPlayer) {
                    // do nothing and temporarily suspends playing
                    // in order to return command to the player
                    // Used to execute a sub-script from java
                    break;
                } else if (range instanceof ActionLoadSbd) {
                    ActionLoadSbd loadSbd = (ActionLoadSbd) range;
                    script = loadSbd.script;
                    resetScript();
                    range = loadSbd;
                    // Jumping into a different script
                    // definitely exits the play range
                    action = getAction();
                }
                if (Thread.interrupted()) {
                    throw new ScriptInterruptedException();
                }
            } catch (ScriptInterruptedException e) {
                // Because script functions in the break range statement are
                // running in the same player instance as the main script,
                // we have to check the play range in order to find out if the
                // onClose handler should be called.
                // It's kind of a hack, and leaves a small loop hole
                // (placing the onClose range inside the play range),
                // but saves us from creating a second player instance
                boolean callOnClose = playRange == null
                        || (playRange.contains(script.onClose.start) && playRange
                                .contains(script.onClose.end));
                if (script.onClose != null && callOnClose && !intentionalQuit) {
                    // Done automatically in reply(), otherwise we have to do it
                    endAll();
                    range = script.onClose;
                    // clear interrupted state
                    Thread.interrupted();
                    // Continue with onClose range
                } else {
                    throw e;
                }
            } catch (ScriptExecutionError e) {
                throw e;
            } catch (Throwable t) {
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
            teaseLib.log.info("All actions set");
            if (script.onAllSet != null && invokedOnAllSet == false) {
                teaseLib.log.info("Invoking OnAllSet handler");
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
        // at least complete their mandatory part.
        // But interactions perform different, for instance
        // Ask displays its user interface while the visuals render,
        // to allow the message to be spoken during checkbox selection.
        // Interactions that eventually call choose(...) do this
        // implicitly, but all other classes like Range have to call it
        // when suitable, to prevent text and messages appearing too fast
        ScriptFunction visuals = new ScriptFunction() {
            @Override
            public void run() {
                if (action.visuals != null) {
                    for (Visual visual : action.visuals.values()) {
                        render(visual);
                    }
                }
            }
        };
        ActionRange range = action.interaction.getRange(script, action,
                visuals, this);
        return range;
    }

    public void render(Visual visual) {
        visual.render(this);
    }

    /**
     * Build a list of all executable actions, after evaluating conditions.
     * 
     * @param range
     * @return List of available actions.
     */
    public List<Action> range(ActionRange range) {
        return range(script, range);
    }

    /**
     * Build a list of all executable actions, after evaluating conditions.
     * 
     * @param script
     * @param range
     * @return List of available actions.
     */
    private List<Action> range(Script script, ActionRange range) {
        List<Action> candidates = script.actions.getAll(range);
        List<Action> selectable = new LinkedList<Action>();
        List<Action> poss0 = null;
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
                            teaseLib.log.info("Action " + action.number + ": "
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
                        if (action.poss == 0) {
                            if (poss0 == null) {
                                poss0 = new LinkedList<Action>();
                            }
                            poss0.add(action);
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
        if (poss0 == null && poss100 == null) {
            return selectable;
        } else if (poss100 != null) {
            // poss == 100 overrides
            return poss100;
        } else if (poss0 != null && selectable.size() == 0) {
            // poss == 1 is last chance
            return poss0;
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
            teaseLib.log.info("Action " + action.number);
        } else {
            // Log code
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
            if (actionList == null) {
                teaseLib.log.info("Action list is empty");
            } else {
                teaseLib.log.info(actionList.toString());
            }
            // Normalize all actions into the interval [0...100], the choose one
            // "poss" 100 is used to implement an "else" clause, since PCM
            // script
            // doesn't have one otherwise
            double normalized = 100.0;
            double accumulatedWeights[] = new double[actions.size()];
            double sum = 0.0;
            // Log weights
            StringBuilder weightList = null;
            for (int i = 0; i < accumulatedWeights.length; i++) {
                Action a = actions.get(i);
                Integer weight = a.poss;
                double relativeWeight = normalized / accumulatedWeights.length;
                sum += weight != null ? weight
                // This would be mathematically correct
                // if none of the action specified a "poss" value
                        : relativeWeight;
                accumulatedWeights[i] = sum;
                String w = String.format("%.2f", relativeWeight);
                if (weightList == null) {
                    weightList = new StringBuilder("Weight:\t" + w);
                } else {
                    weightList.append("\t");
                    weightList.append(w);
                }
            }
            if (weightList != null) {
                teaseLib.log.info(weightList.toString());
            } else {
                teaseLib.log.info("Weight list is empty");
            }
            // Normalize and build interval
            for (int i = 0; i < accumulatedWeights.length; i++) {
                accumulatedWeights[i] *= normalized / sum;
            }
            // Choose a value
            double value = random(0, (int) normalized);
            action = null;
            for (int i = 0; i < accumulatedWeights.length; i++) {
                if (value <= accumulatedWeights[i]) {
                    action = actions.get(i);
                    break;
                }
            }
            if (action == null) {
                action = actions.get(accumulatedWeights.length - 1);
            }
            teaseLib.log.info("Random = " + value + " -> choosing action "
                    + action.number);
        }
        return action;
    }

    @Override
    protected String showChoices(ScriptFunction scriptFunction,
            List<String> choices, Confidence recognitionConfidence) {
        // Display text according to slave's level of articulateness
        if (state.get(script.gag).equals(State.SET)) {
            // Slave is gagged
            final List<String> processedChoices = new ArrayList<String>(
                    choices.size());
            for (String choice : choices) {
                // The simple solution: replace vocals with consonants
                choice = choice.replace("a", "m");
                choice = choice.replace("e", "m");
                choice = choice.replace("i", "m");
                choice = choice.replace("o", "m");
                choice = choice.replace("u", "m");
                processedChoices.add(choice);
            }
            final String processedChoice = super.showChoices(scriptFunction,
                    processedChoices, recognitionConfidence);
            // Return the original choice instance
            int index = processedChoices.indexOf(processedChoice);
            if (index < 0) {
                // Timeout
                return processedChoice;
            } else {
                return choices.get(index);
            }
        } else {
            return super.showChoices(scriptFunction, choices,
                    recognitionConfidence);
        }
    }

    public static void validate(Script script,
            List<ValidationError> validationErrors) {
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

    private void showError(ScriptError e) {
        teaseLib.log.error(this, e);
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
        teaseLib.log.error(this, t);
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
        teaseLib.log.info(error);
        teaseLib.host.show(null, error);
        reply("Oh Dear");
    }
}
