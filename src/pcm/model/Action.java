package pcm.model;

import static java.lang.Integer.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pcm.controller.Declarations;
import pcm.state.Command;
import pcm.state.Condition;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;
import pcm.state.StateCommandLineParameters;
import pcm.state.Validatable;
import pcm.state.ValidatableResources;
import pcm.state.Visual;
import pcm.state.commands.ItemCommand;
import pcm.state.commands.Repeat;
import pcm.state.commands.RepeatAdd;
import pcm.state.commands.RepeatDel;
import pcm.state.commands.Save;
import pcm.state.commands.Set;
import pcm.state.commands.SetTime;
import pcm.state.commands.StateCommand;
import pcm.state.commands.Unset;
import pcm.state.conditions.IfSet;
import pcm.state.conditions.IfState;
import pcm.state.conditions.IfUnset;
import pcm.state.conditions.ItemCondition;
import pcm.state.conditions.Must;
import pcm.state.conditions.MustAnyOf;
import pcm.state.conditions.MustNot;
import pcm.state.conditions.MustNotAllOf;
import pcm.state.conditions.Not;
import pcm.state.conditions.NumActionsAvailable;
import pcm.state.conditions.NumActionsFrom;
import pcm.state.conditions.NumberOfActionsSet;
import pcm.state.conditions.Should;
import pcm.state.conditions.StateCondition;
import pcm.state.conditions.TimeFrom;
import pcm.state.conditions.TimeTo;
import pcm.state.interactions.Ask;
import pcm.state.interactions.Break;
import pcm.state.interactions.Chat;
import pcm.state.interactions.GoSub;
import pcm.state.interactions.LoadSbd;
import pcm.state.interactions.No;
import pcm.state.interactions.Pause;
import pcm.state.interactions.PopUp;
import pcm.state.interactions.Quit;
import pcm.state.interactions.Range;
import pcm.state.interactions.Return;
import pcm.state.interactions.Stop;
import pcm.state.interactions.Stop.TimeoutType;
import pcm.state.interactions.Yes;
import pcm.state.interactions.YesNo;
import pcm.state.visuals.Delay;
import pcm.state.visuals.Exec;
import pcm.state.visuals.Image;
import pcm.state.visuals.KeyReleaseHandler;
import pcm.state.visuals.NoImage;
import pcm.state.visuals.NoMessage;
import pcm.state.visuals.Sound;
import pcm.state.visuals.SpokenMessage;
import pcm.state.visuals.Timeout;
import teaselib.core.speechrecognition.SpeechRecognition.TimeoutBehavior;

public class Action extends AbstractAction {
    public final int number;
    public Integer poss = null;

    public List<Condition> conditions = null;
    public Map<Statement, Visual> visuals = null;
    public Visual message = null;
    public Interaction interaction = null;

    public Action(int n) {
        this.number = n;
    }

    public void finalizeParsing(Script script) throws ValidationIssue {
        if (visuals != null) {
            // Get delay
            Visual visual;
            if (visuals.containsKey(Statement.Delay) == true) {
                visual = visuals.get(Statement.Delay);
            } else if (visuals.containsKey(Statement.ActionDelay) == true) {
                // ActionDelay is mapped to Delay statement, and may not appear in action visuals
                throw new IllegalStateException(Statement.ActionDelay.toString());
            } else {
                visual = null;
            }

            // TODO Re-enable check
            // if (hasMessageStatement && hasTxt)
            // throw new ValidationIssue(this,
            // "Spoken messages and .txt are exclusive because PCMPlayer/TeaseLib supports only one text area",
            // script);

            boolean hasMessage = message != null;

            boolean hasDelay;
            if (visual instanceof Delay) {
                hasDelay = ((Delay) visual).from > 0;
            } else {
                // Since the script ends with Quit, the delay is actually infinite
                hasDelay = hasMessage;
            }
            if (hasDelay) {
                // Add empty message in order to render NoImage + NoMessage
                if (!hasMessage) {
                    message = NoMessage.instance;
                }
            } else {
                if (visuals.containsKey(Statement.NoImage)) {
                    // .delay 0 + .noimage actions are compute actions,
                    // So without a delay, nothing should be rendered
                    visuals.remove(Statement.NoImage);
                }
                if (visuals.containsKey(Statement.Message)) {
                    // Without a delay, or delay == 0,
                    // nothing would be rendered anyway
                    visuals.remove(Statement.Message);
                }
                if (visuals.containsKey(Statement.Image)) {
                    // Otherwise, the image would be displayed in the next
                    // action with a message, which would be incorrect
                    // In the original PCMistress program,
                    // such images wouldn't be rendered at all,
                    // or just pop up for a fraction of a second
                    throw new ValidationIssue(this,
                            "Without a message, a Delay statement is needed to display the image", script);
                }
            }
        }
    }

    public void addCondition(Condition condition) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        conditions.add(condition);
    }

    public void addVisual(Statement statement, Visual visual) {
        if (this.visuals == null) {
            this.visuals = new LinkedHashMap<>();
        }
        visuals.put(statement, visual);
    }

    /*
     * (non-Javadoc)
     * 
     * @see pcm.model.AbstractAction#add(pcm.model.ScriptLineTokenizer)
     */
    @Override
    public void add(ScriptLineTokenizer cmd) throws ScriptParsingException {
        final Statement name = cmd.statement;
        if (name == Statement.NoImage) {
            addVisual(name, NoImage.instance);
        } else if (name == Statement.Image) {
            addVisual(name, new Image(cmd.asFilename()));
        } else if (name == Statement.PlayWav) {
            addVisual(name, new Sound(cmd.asFilename()));
        } else if (name == Statement.Exec) {
            addVisual(name, new Exec(cmd.asFilename()));
        } else if (name == Statement.Message) {
            throw new IllegalStateException(name.toString());
        } else if (name == Statement.Txt) {
            throw new IllegalStateException(name.toString());
        } else if (name == Statement.Delay || name == Statement.ActionDelay) {
            String args[] = cmd.args();
            if (args.length == 1) {
                // delay
                int delay = parseInt(args[0]);
                addVisual(Statement.Delay, new Delay(delay));
            } else if (args.length == 2) {
                addDelay(args);
            } else if (args.length >= 4) {
                addDelayAndReplies(cmd, args);
            } else {
                throw new IllegalArgumentException(cmd.line);
            }
        } else if (name == Statement.KeyRelease) {
            Visual keyRelease = null;
            String args[] = cmd.args();
            String command = args[0];
            for (String string : KeyReleaseHandler.Commands) {
                if (string.equalsIgnoreCase(command)) {
                    if (args.length == 1) {
                        keyRelease = new KeyReleaseHandler(command);
                        break;
                    } else if (args.length == 2) {
                        String durationArg = args[1];
                        long duration;
                        if ("min".equalsIgnoreCase(durationArg)) {
                            duration = Long.MIN_VALUE;
                        } else if ("max".equalsIgnoreCase(durationArg)) {
                            duration = Long.MAX_VALUE;
                        } else {
                            duration = parseInt(durationArg);
                        }
                        keyRelease = new KeyReleaseHandler(command, duration);
                        break;
                    } else {
                        throw new IllegalArgumentException(cmd.line);
                    }
                }
            }
            if (keyRelease == null) {
                throw new IllegalArgumentException(cmd.line);
            } else {
                addVisual(name, keyRelease);
            }
        } else if (name == Statement.Say) {
            // Ignore, because message are spoken per default
        }
        // Conditions
        else if (name == Statement.Must) {
            Must must = new Must();
            cmd.addArgsTo(must);
            addCondition(must);
        } else if (name == Statement.MustNot) {
            MustNot mustNot = new MustNot();
            cmd.addArgsTo(mustNot);
            addCondition(mustNot);
        } else if (name == Statement.MustNotAllOf) {
            MustNotAllOf mustNotAllOf = new MustNotAllOf();
            cmd.addArgsTo(mustNotAllOf);
            addCondition(mustNotAllOf);
        } else if (name == Statement.MustAnyOf) {
            MustAnyOf mustAnyOf = new MustAnyOf();
            cmd.addArgsTo(mustAnyOf);
            addCondition(mustAnyOf);
        } else if (name == Statement.Should) {
            String arg0 = cmd.args()[0];
            if (AbstractAction.Statement.Lookup.containsKey(cmd.args()[0].substring(1))) {
                Condition condition = createConditionFrom(cmd.lineNumber, cmd.argsFrom(0), cmd.script,
                        cmd.declarations);
                Condition should = new Should(condition);
                addCondition(should);
            } else if (isNumeric(arg0)) {
                Must must = new Must();
                cmd.addArgsTo(must);
                Condition should = new Should(must);
                addCondition(should);
            } else {
                throw new IllegalStatementException(name, cmd.allArgs());
            }
        } else if (name == Statement.ShouldNot) {
            String arg0 = cmd.args()[0];
            if (AbstractAction.Statement.Lookup.containsKey(cmd.args()[0].substring(1))) {
                Condition condition = createConditionFrom(cmd.lineNumber, cmd.argsFrom(0), cmd.script,
                        cmd.declarations);
                Condition not = new Not(condition);
                Condition should = new Should(not);
                addCondition(should);
            } else if (isNumeric(arg0)) {
                MustNot mustNot = new MustNot();
                cmd.addArgsTo(mustNot);
                Condition should = new Should(mustNot);
                addCondition(should);
            } else {
                throw new IllegalStatementException(name, cmd.allArgs());
            }
        }
        // Commands
        else if (name == Statement.Set) {
            Set set = new Set();
            cmd.addArgsTo(set);
            addCommand(set);
        } else if (name == Statement.UnSet) {
            Unset unset = new Unset();
            cmd.addArgsTo(unset);
            addCommand(unset);
        } else if (name == Statement.SetTime) {
            String args[] = cmd.args();
            if (args.length == 1) {
                addCommand(new SetTime(parseInt(args[0])));
            } else {
                addCommand(new SetTime(parseInt(args[0]), args[1]));
            }
        } else if (name == Statement.SetRange) {
            throw new IllegalStateException("Validate functionality of SetRange in PCMistress first");
        } else if (name == Statement.TimeFrom) {
            String args[] = cmd.args();
            addCondition(new TimeFrom(parseInt(args[0]), args[1]));
        } else if (name == Statement.TimeTo) {
            String args[] = cmd.args();
            addCondition(new TimeTo(parseInt(args[0]), args[1]));
        } else if (name == Statement.NumActionsFrom) {
            String args[] = cmd.args();
            addCondition(new NumActionsFrom(parseInt(args[0]), parseInt(args[1])));
        } else if (name == Statement.NumberOfActionsSet) {
            String args[] = cmd.args();
            if (args.length == 3) {
                addCondition(new NumberOfActionsSet(parseInt(args[0]), parseInt(args[1]), parseInt(args[2])));
            } else {
                throw new IllegalArgumentException(cmd.line);
            }
        } else if (name == Statement.NumActionsAvailable) {
            String args[] = cmd.args();
            if (args.length == 3) {
                addCondition(new NumActionsAvailable(parseInt(args[0]), parseInt(args[1]), parseInt(args[2])));
            } else {
                throw new IllegalArgumentException(cmd.line);
            }
        } else if (name == Statement.Not) {
            addCondition(new Not(createConditionFrom(cmd.lineNumber, cmd.argsFrom(0), cmd.script, cmd.declarations)));
        } else if (name == Statement.Repeat) {
            String args[] = cmd.args();
            if (args.length == 1) {
                addCommand(new Repeat(number, parseInt(args[0])));
            } else {
                throw new IllegalArgumentException(cmd.line);
            }
        } else if (name == Statement.RepeatAdd) {
            final Command repeatAdd;
            String args[] = cmd.args();
            if (args.length == 1) {
                repeatAdd = new RepeatAdd(parseInt(args[0]));
            } else if (args.length == 3) {
                repeatAdd = new RepeatAdd(parseInt(args[0]), parseInt(args[1]), parseInt(args[2]));
            } else {
                throw new IllegalArgumentException(cmd.line);
            }
            addCommand(repeatAdd);
        } else if (name == Statement.RepeatDel) {
            final Command repeatDel;
            String args[] = cmd.args();
            if (args.length == 1) {
                repeatDel = new RepeatDel(parseInt(args[0]));
            } else if (args.length == 3) {
                repeatDel = new RepeatDel(parseInt(args[0]), parseInt(args[1]), parseInt(args[2]));
            } else {
                throw new IllegalArgumentException(cmd.line);
            }
            addCommand(repeatDel);
        } else if (name == Statement.Save) {
            String args[] = cmd.args();
            addCommand(new Save(parseInt(args[0]), parseInt(args[1])));
        } else if (name == Statement.Poss) {
            if (poss != null) {
                throw new IllegalArgumentException(".poss and .else statements cannot be used simultanously");
            } else {
                String args[] = cmd.args();
                poss = Integer.valueOf(args[0]);
                if (poss < 1 || poss > 100) {
                    throw new IllegalArgumentException(cmd.line);
                }
            }
        } else if (name == Statement.Else) {
            if (poss != null) {
                throw new IllegalArgumentException(".poss and .else statements cannot be used simultanously");
            } else {
                poss = 0;
            }
        } else if (name == Statement.IfSet) {
            String args[] = cmd.args();
            int n = parseInt(args[0]);
            Command conditional = createCommandFrom(cmd.lineNumber, cmd.argsFrom(1), cmd.script, cmd.declarations);
            Command ifSet = new IfSet(n, conditional);
            addCommand(ifSet);
        } else if (name == Statement.IfUnset) {
            String args[] = cmd.args();
            int n = parseInt(args[0]);
            Command conditional = createCommandFrom(cmd.lineNumber, cmd.argsFrom(1), cmd.script, cmd.declarations);
            Command ifUnset = new IfUnset(n, conditional);
            addCommand(ifUnset);
        } else if (name == Statement.State) {
            StateCommandLineParameters args = new StateCommandLineParameters(cmd.args(), cmd.declarations);
            if (args.isCommand()) {
                addCommand(new StateCommand(args));
            } else {
                if (IfState.isExtendedIfClause(cmd.args())) {
                    addCondition(new IfState(name, cmd.args(), new IfState.ConditionCreator() {
                        @Override
                        public Condition createCondition(StateCommandLineParameters firstParameters)
                                throws ScriptParsingException {
                            return new StateCondition(firstParameters);
                        }
                    }, cmd.declarations));
                } else {
                    addCondition(new StateCondition(args));
                }
            }
        } else if (name == Statement.Item) {
            StateCommandLineParameters args = new StateCommandLineParameters(cmd.args(), cmd.declarations);
            if (args.isCommand()) {
                addCommand(new ItemCommand(args));
            } else {
                if (IfState.isExtendedIfClause(cmd.args())) {
                    addCondition(new IfState(name, cmd.args(), new IfState.ConditionCreator() {

                        @Override
                        public Condition createCondition(StateCommandLineParameters firstParameters)
                                throws ScriptParsingException {
                            return new ItemCondition(firstParameters);
                        }
                    }, cmd.declarations));
                } else {
                    addCondition(new ItemCondition(args));
                }
            }
        }
        // interactions
        else if (name == Statement.Range) {

            String args[] = cmd.args();
            int start = parseInt(args[0]);
            if (args.length > 1) {
                int end = parseInt(args[1]);
                setInteraction(new Range(start, end));
            } else {
                setInteraction(new Range(start));
            }
        } else if (name == Statement.Pause) {
            String resumeText = cmd.allAsText();
            if (resumeText.isEmpty()) {
                resumeText = getResponseText(Statement.ResumeText, cmd.script);
            }

            setInteraction(new Pause(resumeText));
        } else if (name == Statement.Yes)

        {
            String yesText = cmd.allAsText();
            if (yesText.isEmpty()) {
                yesText = getResponseText(Statement.YesText, cmd.script);
            }
            setInteraction(new Yes(yesText));
        } else if (name == Statement.No) {
            String noText = cmd.allAsText();
            if (noText.isEmpty()) {
                noText = getResponseText(Statement.NoText, cmd.script);
            }
            setInteraction(new No(noText));
        } else if (name == Statement.Chat) {
            String chatText = cmd.allAsText();
            if (chatText.isEmpty()) {
                chatText = getResponseText(Statement.ChatText, cmd.script);
            }
            setInteraction(new Chat(chatText));
        } else if (name == Statement.YesNo) {
            String args[] = cmd.args();
            setInteraction(new YesNo(parseInt(args[0]), parseInt(args[1]), parseInt(args[2]), parseInt(args[3])));
        } else if (name == Statement.LoadSbd) {
            String args[] = cmd.args();
            String arg0 = args[0];
            int endIndex = arg0.lastIndexOf('.');
            String script = endIndex < 0 ? arg0 : arg0.substring(0, endIndex);
            setInteraction(args.length > 2 ? new LoadSbd(script, parseInt(args[1]), parseInt(args[2]))
                    : new LoadSbd(script, parseInt(args[1])));
        } else if (name == Statement.PopUp) {
            String args[] = cmd.args();
            setInteraction(new PopUp(parseInt(args[0]), parseInt(args[1])));
        } else if (name == Statement.Ask) {
            String args[] = cmd.args();
            Ask ask = new Ask(parseInt(args[0]), parseInt(args[1]));
            // Ask must also be a command, in order to pick up the state
            addCommand(ask);
            setInteraction(ask);
        } else if (name == Statement.Quit) {
            setInteraction(Quit.instance);
        } else if (name == Statement.Break) {
            String args[] = cmd.args();
            ActionRange playRange = new ActionRange(parseInt(args[0]), parseInt(args[1]));
            if (args[2].equalsIgnoreCase(Break.SuppressStackCorrectionOnBreak)) {
                setInteraction(new Break(playRange, rangesFromArgv(args, 3), true));
            } else {
                setInteraction(new Break(playRange, rangesFromArgv(args, 2), false));
            }
        } else if (name == Statement.GoSub) {
            String args[] = cmd.args();
            int start = parseInt(args[0]);
            if (args.length > 1) {
                int end = parseInt(args[1]);
                setInteraction(new GoSub(new ActionRange(start, end)));
            } else {
                setInteraction(new GoSub(new ActionRange(start)));
            }
        } else if (name == Statement.Return) {
            setInteraction(new Return());
        } else if (name == Statement.Append) {
            // TODO
        } else if (name == Statement.Prepend) {
            // TODO
        } else if (name == Statement.Replace) {
            // TODO
        } else {
            super.add(cmd);
        }
    }

    private void addDelayAndReplies(ScriptLineTokenizer cmd, String[] args) {
        int from = parseInt(args[0]);
        int to = parseInt(args[1]);
        // Type
        TimeoutType timeoutType = null;
        final String arg2 = args[2].toLowerCase();
        if (arg2.equals("confirm")) {
            timeoutType = Stop.TimeoutType.Confirm;
        } else if (arg2.equals("autoconfirm")) {
            timeoutType = Stop.TimeoutType.AutoConfirm;
        } else if (arg2.equals("terminate")) {
            timeoutType = TimeoutType.Terminate;
        }
        // Behavior
        TimeoutBehavior timeoutBehavior = null;
        final String arg3 = args[3].toLowerCase();
        if (arg3.equals("indubiomitius")) {
            timeoutBehavior = TimeoutBehavior.InDubioMitius;
        } else if (arg3.equals("indubioproduriore")) {
            timeoutBehavior = TimeoutBehavior.InDubioProDuriore;
        } else if (arg3.equals("indubiocontrareum")) {
            timeoutBehavior = TimeoutBehavior.InDubioContraReum;
        }
        // Build the statement
        if (timeoutType != null && timeoutBehavior != null) {
            addVisual(Statement.Delay, new Timeout(from, to));
            setInteraction(new Stop(rangesFromArgv(args, 4), timeoutType, timeoutBehavior));
        } else if (timeoutType != null && timeoutBehavior == null) {
            addVisual(Statement.Delay, new Timeout(from, to));
            setInteraction(new Stop(rangesFromArgv(args, 3), timeoutType, TimeoutBehavior.InDubioProDuriore));
        } else if (timeoutType == null && timeoutBehavior == null) {
            addVisual(Statement.Delay, new Delay(from, to));
            setInteraction(
                    new Stop(rangesFromArgv(args, 2), Stop.TimeoutType.Terminate, TimeoutBehavior.InDubioProDuriore));
        } else {
            throw new IllegalArgumentException(cmd.line);
        }
    }

    private void addDelay(String[] args) {
        int from = parseInt(args[0]);
        int to = parseInt(args[1]);
        addVisual(Statement.Delay, new Delay(from, to));
    }

    private static boolean isNumeric(String string) {
        return string.matches("[0-9]+");
    }

    private static Command createCommandFrom(int lineNumber, String line, Script script, Declarations declarations)
            throws ScriptParsingException {
        ScriptLineTokenizer cmd = new ScriptLineTokenizer(lineNumber, line, script, declarations);
        Action action = new Action(0);
        action.add(cmd);
        return action.commands.get(0);
    }

    /**
     * Parse the remainder of argv into a sequence of action ranges
     * 
     * @param args
     *            Arguments
     * @param index
     *            Start index.
     * @return
     */
    private static Map<Statement, ActionRange> rangesFromArgv(String[] args, int index) {
        Map<Statement, ActionRange> ranges = new LinkedHashMap<>();
        while (index < args.length) {
            String keyword = args[index++];
            ActionRange actionRange;
            int start = parseInt(args[index++]);
            if (index < args.length) {
                // More parameters
                try {
                    int end = parseInt(args[index]);
                    index++;
                    actionRange = new ActionRange(start, end);
                } catch (NumberFormatException e) {
                    // Next keyword
                    actionRange = new ActionRange(start);
                }
            } else {
                // Single value range
                actionRange = new ActionRange(start);
            }
            Statement key = keywordToStatement(keyword);
            ranges.put(key, actionRange);
        }
        return ranges;
    }

    private static Statement keywordToStatement(String keyword) {
        return Statement.KeywordToStatement.get(keyword.toLowerCase());
    }

    private void setInteraction(Interaction interaction) {
        if (this.interaction != null) {
            if (this.interaction instanceof NeedsRangeProvider) {
                // For instance delay + range
                ((NeedsRangeProvider) this.interaction).setRangeProvider(interaction);
            } else {
                if (interaction instanceof NeedsRangeProvider) {
                    // For instance injection of delay -> interaction had been
                    // set to range already on parsing the script
                    ((NeedsRangeProvider) interaction).setRangeProvider(this.interaction);
                    this.interaction = interaction;
                } else {
                    // Only actions that need a range provide can be injected
                    // before an existing interaction
                    throw new IllegalArgumentException(interaction.getClass().getSimpleName()
                            + ": Interaction already set to " + this.interaction.getClass().getSimpleName());
                }
            }
        } else {
            this.interaction = interaction;
        }
    }

    public String getResponseText(Statement name, Script script) {
        if (responses != null) {
            if (responses.containsKey(name)) {
                return responses.get(name);
            }
        }
        return script.getResponseText(name);
    }

    public void validate(Script script, List<ValidationIssue> validationErrors) throws ScriptParsingException {
        if (poss != null) {
            if (poss < 0 || poss > 100) {
                validationErrors
                        .add(new ValidationIssue(this, "Invalid value for poss statement (" + poss + ")", script));
            }
        }
        if (visuals != null) {
            // Validate each visual
            for (Visual visual : visuals.values()) {
                if (visual instanceof Validatable) {
                    ((Validatable) visual).validate(script, this, validationErrors);
                }
            }

            if (visuals.containsKey(Statement.Message)) {
                addMessageValidationIssue(script, validationErrors, Statement.Message);
            } else if (visuals.containsKey(Statement.Txt)) {
                addMessageValidationIssue(script, validationErrors, Statement.Txt);
            }

            if (message == null) {
                Statement[] requiresMessage = { Statement.Image, Statement.Delay };
                for (Statement statement : requiresMessage) {
                    if (visuals.containsKey(statement)) {
                        validationErrors.add(new ValidationIssue(this,
                                "Message without .txt or speech part won't render images or other media", script));
                    }
                }
            } else if (message instanceof SpokenMessage) {
                if (((SpokenMessage) message).getMessages().size() > 1) {
                    Statement[] requiresSinglePageMessage = { Statement.Image, Statement.Delay };
                    for (Statement statement : requiresSinglePageMessage) {
                        if (visuals.containsKey(statement)) {
                            validationErrors.add(new ValidationIssue(this,
                                    "." + statement.toString() + "  doesn't work correctly in multi-page messages",
                                    script));
                        }
                    }
                }
            }

            // TODO Move this check to collectors
            // } else if (visuals.containsKey(Statement.Txt) && visuals.containsKey(Statement.Message)) {
            // validationErrors.add(new ValidationIssue(this, "Both .txt and message is not supported", script));

            // .delay 0 & .noimage
            if (visuals.containsKey(Statement.Delay) && visuals.containsKey(Statement.NoImage)) {
                Delay delay = (Delay) visuals.get(Statement.Delay);
                if (delay.to == 0) {
                    validationErrors.add(
                            new ValidationIssue(this, "Delay 0 + NoImage is deprecated and should be removed", script));
                }
            } else if (visuals.containsKey(Statement.Delay)) {
                Delay delay = (Delay) visuals.get(Statement.Delay);
                if (delay.to == 0) {
                    validationErrors
                            .add(new ValidationIssue(this, "Delay 0 is deprecated and should be removed", script));
                }
            }
        }
        if (interaction != null) {
            interaction.validate(script, this, validationErrors);
        } else {
            validationErrors.add(new ValidationIssue(this, "No interaction", script));
        }
    }

    private void addMessageValidationIssue(Script script, List<ValidationIssue> validationErrors, Statement special) {
        validationErrors
                .add(new ValidationIssue(this, special + " is must be rendered last - remove it from visuals", script));
    }

    public List<String> validateResources() {
        List<String> resources = new ArrayList<>();
        if (visuals != null) {
            for (Visual visual : visuals.values()) {
                if (visual instanceof ValidatableResources) {
                    resources.addAll(((ValidatableResources) visual).resources());
                }
            }
        }
        return resources;
    }

    @Override
    public String toString() {
        return "Action " + number;
    }
}
