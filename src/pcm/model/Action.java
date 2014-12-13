package pcm.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import pcm.state.Command;
import pcm.state.Condition;
import pcm.state.Interaction;
import pcm.state.Interaction.NeedsRangeProvider;
import pcm.state.Visual;
import pcm.state.Interactions.Ask;
import pcm.state.Interactions.AskYesNo;
import pcm.state.Interactions.Break;
import pcm.state.Interactions.LoadSbd;
import pcm.state.Interactions.Pause;
import pcm.state.Interactions.PopUp;
import pcm.state.Interactions.Quit;
import pcm.state.Interactions.Range;
import pcm.state.Interactions.Stop;
import pcm.state.commands.Repeat;
import pcm.state.commands.RepeatAdd;
import pcm.state.commands.RepeatDel;
import pcm.state.commands.Save;
import pcm.state.commands.SetTime;
import pcm.state.commands.Unset;
import pcm.state.conditions.Must;
import pcm.state.conditions.MustNot;
import pcm.state.conditions.NumActionsFrom;
import pcm.state.conditions.TimeFrom;
import pcm.state.visuals.Delay;
import pcm.state.visuals.Exec;
import pcm.state.visuals.Image;
import pcm.state.visuals.MistressImage;
import pcm.state.visuals.NoImage;
import pcm.state.visuals.NoMessage;
import pcm.state.visuals.Sound;
import pcm.state.visuals.Txt;

public class Action extends AbstractAction {
    public final int number;
    public Integer poss = null;
    public boolean say = false;

    public Vector<Condition> conditions = null;
    public LinkedHashMap<Statement, Visual> visuals = null;

    public Interaction interaction = null;

    public Action(int n) {
        this.number = n;
    }

    public void finalizeParsing() throws ValidationError {
        if (visuals != null) {
            // Get delay
            final Visual visual;
            if (visuals.containsKey(Statement.Delay) == true) {
                visual = visuals.get(Statement.Delay);
            } else if (visuals.containsKey(Statement.ActionDelay) == true) {
                visual = visuals.get(Statement.ActionDelay);
            } else {
                visual = null;
            }
            // Message or txt
            final boolean hasMessageStatement = visuals
                    .containsKey(Statement.Message);
            final boolean hasTxtStatement = visuals.containsKey(Statement.Txt);
            if (hasMessageStatement && hasTxtStatement)
                throw new ValidationError(
                        this,
                        "Spoken messages and .txt are exclusive because PCMPlayer/TeaseLib supports only one text area");
            final boolean hasMessage = hasMessageStatement || hasTxtStatement;
            final boolean hasDelay;
            if (visual instanceof Delay) {
                hasDelay = ((Delay) visual).from > 0;
            } else if (visual instanceof ActionDelay) {
                hasDelay = ((ActionDelay) visual).from > 0;
            } else {
                // Since the script ends with Quit, the delay is actually
                // infinite
                hasDelay = hasMessage;
            }
            if (hasDelay) {
                if (!visuals.containsKey(Statement.Image)
                        && !visuals.containsKey(Statement.NoImage)) {
                    addVisual(Statement.Image, MistressImage.instance);
                }
                if (!hasMessage) {
                    addVisual(Statement.Message, NoMessage.instance);
                }
            } else {
                if (visuals.containsKey(Statement.NoImage)) {
                    visuals.remove(Statement.NoImage);
                }
                if (visuals.containsKey(Statement.Message)) {
                    visuals.remove(Statement.Message);
                }
            }
        }
    }

    public void addCondition(Condition condition) {
        if (this.conditions == null) {
            this.conditions = new Vector<>();
        }
        conditions.add(condition);
    }

    public void addVisual(Statement statement, Visual visual) {
        if (this.visuals == null) {
            this.visuals = new LinkedHashMap<>();
        }
        visuals.put(statement, visual);
    }

    public void addTxt(String txtLine) {
        if (visuals == null) {
            addVisual(Statement.Txt, new Txt(txtLine));
        } else if (visuals.containsKey(Statement.Txt)) {
            Txt txt = (Txt) visuals.get(Statement.Txt);
            txt.add(txtLine);
        } else {
            addVisual(Statement.Txt, new Txt(txtLine));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see pcm.model.AbstractAction#add(pcm.model.ScriptLineTokenizer)
     */
    @Override
    public void add(ScriptLineTokenizer cmd) throws ParseError {
        final Statement name = cmd.statement;
        // Some visuals depend on other statements, and thus must appear in
        // the correct order in the sbd-script.
        // This has been overseen when designing the parser. As a result,
        // these dependencies have been modeled by throwing ParseExceptions, but
        // the order of appearence in the script can easily be changed.
        // Since Mine seems the only script worth porting, this will not be
        // fixed here.
        // Affected statements:
        // - noimage & txt
        // - image & txt
        // noimage and message, but the message is collected by the parser, not
        // as a statement, therefore a special case and it works
        if (name == Statement.NoImage) {
            if (visuals != null && visuals.containsKey(Statement.Txt)) {
                throw new ParseError(cmd.lineNumber, number, cmd.all(),
                        ".noimage statement must precede .txt");
            }
            addVisual(name, NoImage.instance);
        } else if (name == Statement.Image) {
            if (visuals != null && visuals.containsKey(Statement.Txt)) {
                throw new ParseError(cmd.lineNumber, number, cmd.all(),
                        ".image statement must precede .txt");
            }
            addVisual(name, new Image(cmd.allArgs()));
        } else if (name == Statement.PlayWav) {
            addVisual(name, new Sound(cmd.allArgs()));
        } else if (name == Statement.Exec) {
            addVisual(name, new Exec(cmd.allArgs()));
        } else if (name == Statement.Message) {
            throw new IllegalStateException(name.toString());
        } else if (name == Statement.Txt) {
            addTxt(cmd.all());
        } else if (name == Statement.Pause) {
            setInteraction(new Pause());
        } else if (name == Statement.Delay || name == Statement.ActionDelay) {
            String args[] = cmd.args();
            if (args.length == 1) {
                if (name == Statement.Delay) {
                    // Ignore delay, instead the speech duration is used
                    // actionDelay = new ActionDelay(0);
                } else {
                    int delay = Integer.parseInt(args[0]);
                    if (name == Statement.ActionDelay) {
                        // actionDelay = new ActionDelay(delay);
                        addVisual(name, new Delay(delay));
                    } else {
                        if (delay == 0) {
                            // Treated as "compute"-action, usually in
                            // combination
                            // with .noimage,
                            // because in the original PCMistress, the image
                            // would
                            // otherwise still be loaded
                            // actionDelay = new ActionDelay(delay);
                            addVisual(Statement.Image, NoImage.instance);
                        } else {
                            // Ignored, delay is calculated based on speech or
                            // message length
                        }
                    }
                }
            } else if (args.length == 2) {
                if (name == Statement.Delay) {
                    // Ignore delay, instead the speech duration is used
                } else {
                    addVisual(name, new Delay(Integer.parseInt(args[0]),
                            Integer.parseInt(args[1])));
                }
            } else if (args.length == 4) {
                addVisual(
                        Statement.ActionDelay,
                        new Delay(Integer.parseInt(args[0]), Integer
                                .parseInt(args[1])));
                setInteraction(new Stop(new ActionRange(
                        Integer.parseInt(args[3]))));
            } else if (args.length == 5) {
                addVisual(
                        Statement.ActionDelay,
                        new Delay(Integer.parseInt(args[0]), Integer
                                .parseInt(args[1])));
                setInteraction(new Stop(new ActionRange(
                        Integer.parseInt(args[3]), Integer.parseInt(args[4]))));
            } else {
                throw new IllegalArgumentException(cmd.toString());
            }
        } else if (name == Statement.Say) {
            say = true;
        }
        // Commands
        else if (name == Statement.Must) {
            Must must = new Must();
            for (String arg : cmd.args()) {
                must.add(new Integer(arg));
            }
            addCondition(must);
        } else if (name == Statement.MustNot) {
            MustNot mustNot = new MustNot();
            for (String arg : cmd.args()) {
                mustNot.add(new Integer(arg));
            }
            addCondition(mustNot);
        } else if (name == Statement.Set) {
            pcm.state.commands.Set set = new pcm.state.commands.Set();
            for (String arg : cmd.args()) {
                set.add(new Integer(arg));
            }
            addCommand(set);
        } else if (name == Statement.UnSet) {
            Unset unset = new Unset();
            for (String arg : cmd.args()) {
                unset.add(new Integer(arg));
            }
            addCommand(unset);
        } else if (name == Statement.SetTime) {
            String args[] = cmd.args();
            addCommand(new SetTime(Integer.parseInt(args[0])));
        } else if (name == Statement.SetRange) {
            throw new IllegalStateException(
                    "Validate functionality of SetRange in PCMistress first");
        } else if (name == Statement.TimeFrom) {
            String args[] = cmd.args();
            addCondition(new TimeFrom(Integer.parseInt(args[0]), args[1]));
        } else if (name == Statement.NumActionsFrom) {
            String args[] = cmd.args();
            addCondition(new NumActionsFrom(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1])));
        } else if (name == Statement.Repeat) {
            final Command repeat;
            String args[] = cmd.args();
            if (args.length == 1) {
                repeat = new Repeat(number, Integer.parseInt(args[0]));
            } else {
                throw new IllegalArgumentException();
            }
            addCommand(repeat);
        } else if (name == Statement.RepeatAdd) {
            final Command repeatAdd;
            String args[] = cmd.args();
            if (args.length == 1) {
                repeatAdd = new RepeatAdd(Integer.parseInt(args[0]));
            } else if (args.length == 3) {
                repeatAdd = new RepeatAdd(Integer.parseInt(args[0]),
                        Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            } else {
                throw new IllegalArgumentException();
            }
            addCommand(repeatAdd);
        } else if (name == Statement.RepeatDel) {
            final Command repeatDel;
            String args[] = cmd.args();
            if (args.length == 1) {
                repeatDel = new RepeatDel(Integer.parseInt(args[0]));
            } else if (args.length == 3) {
                repeatDel = new RepeatDel(Integer.parseInt(args[0]),
                        Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            } else {
                throw new IllegalArgumentException();
            }
            addCommand(repeatDel);
        } else if (name == Statement.Save) {
            String args[] = cmd.args();
            addCommand(new Save(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1])));
        } else if (name == Statement.Poss) {
            String args[] = cmd.args();
            poss = new Integer(args[0]);
        }
        // interactions
        else if (name == Statement.Range) {
            String args[] = cmd.args();
            int start = Integer.parseInt(args[0]);
            if (args.length > 1) {
                int end = Integer.parseInt(args[1]);
                setInteraction(new Range(start, end));
            } else {
                setInteraction(new Range(start));
            }
        } else if (name == Statement.YesNo) {
            String args[] = cmd.args();
            setInteraction(new AskYesNo(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                    Integer.parseInt(args[3])));
        } else if (name == Statement.LoadSbd) {
            String args[] = cmd.args();
            String arg0 = args[0];
            int endIndex = arg0.lastIndexOf('.');
            String script = endIndex < 0 ? arg0 : arg0.substring(0, endIndex);
            setInteraction(args.length > 2 ? new LoadSbd(script,
                    Integer.parseInt(args[1]), Integer.parseInt(args[2]))
                    : new LoadSbd(script, Integer.parseInt(args[1])));
        } else if (name == Statement.PopUp) {
            String args[] = cmd.args();
            setInteraction(new PopUp(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1])));
        } else if (name == Statement.Ask) {
            String args[] = cmd.args();
            Ask ask = new Ask(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]));
            // Ask must also be a command, in order to pick up the state
            addCommand(ask);
            setInteraction(ask);
        } else if (name == Statement.Quit) {
            setInteraction(Quit.instance);
        } else if (name == Statement.Break) {
            String args[] = cmd.args();
            setInteraction(new Break(new ActionRange(Integer.parseInt(args[0]),
                    Integer.parseInt(args[1])), rangesFromArgv(args, 2)));
        } else {
            super.add(cmd);
        }
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
    private Map<String, ActionRange> rangesFromArgv(String[] args, int index) {
        Map<String, ActionRange> ranges = new LinkedHashMap<>();
        while (index < args.length) {
            String key = args[index++];
            ActionRange actionRange;
            int start = Integer.parseInt(args[index++]);
            if (index < args.length) {
                // More parameters
                try {
                    int end = Integer.parseInt(args[index]);
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
            ranges.put(key, actionRange);
        }
        return ranges;
    }

    private void setInteraction(Interaction interaction) {
        if (this.interaction != null) {
            if (this.interaction instanceof NeedsRangeProvider) {
                // For instance delay + range
                ((NeedsRangeProvider) this.interaction)
                        .setRangeProvider(interaction);
            } else {
                if (interaction instanceof NeedsRangeProvider) {
                    // For instance injection of delay -> interaction had been
                    // set to range already on parsing the script
                    ((NeedsRangeProvider) interaction)
                            .setRangeProvider(this.interaction);
                    this.interaction = interaction;
                } else {
                    // Only actions that need a range provide can be injected
                    // before an existing interaction
                    throw new IllegalArgumentException(interaction.getClass()
                            .getSimpleName()
                            + ": Interaction already set to "
                            + this.interaction.getClass().getSimpleName());
                }
            }
        } else {
            this.interaction = interaction;
        }
    }

    public String getResponseText(Statement name, Script script)
            throws ScriptExecutionError {
        if (responses != null) {
            if (responses.containsKey(name)) {
                return responses.get(name);
            }
        }
        return script.getResponseText(name);
    }

    public void validate(Script script, List<ValidationError> validationErrors)
            throws ParseError {
        if (poss != null) {
            if (poss == 0 || poss > 100) {
                validationErrors.add(new ValidationError(this,
                        "Invalid value for poss statement (" + poss + ")"));
            }
        }
        // TODO no .noimage + .delay 0
        // TODO Only actiondelay shopuld remain
        if (visuals != null) {
            if (say == true && visuals.containsKey(Statement.Txt)) {
                validationErrors.add(new ValidationError(this,
                        "Can't speak .txt"));
            }
            if (say == true && !visuals.containsKey(Statement.Message)) {
                validationErrors.add(new ValidationError(this,
                        "Unexpected .say without message"));
            }
            if (say == false && visuals.containsKey(Statement.Message)) {
                validationErrors.add(new ValidationError(this,
                        "Must use .txt to display quiet message"));
            }
            if (visuals.containsKey(Statement.Txt)
                    && visuals.containsKey(Statement.Message)) {
                validationErrors.add(new ValidationError(this,
                        "Both .txt and message is supported"));
            }
        }
        if (interaction != null) {
            try {
                interaction.validate(script, this, validationErrors);
            } catch (Exception e) {
                validationErrors.add(new ValidationError(this, e.getMessage()));
            }
        } else {
            validationErrors.add(new ValidationError(this, "No interaction"));
        }
    }

    @Override
    public String toString() {
        return "Action " + number;
    }
}
