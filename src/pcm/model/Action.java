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
import pcm.state.Interactions.GoSub;
import pcm.state.Interactions.LoadSbd;
import pcm.state.Interactions.Pause;
import pcm.state.Interactions.PopUp;
import pcm.state.Interactions.Quit;
import pcm.state.Interactions.Range;
import pcm.state.Interactions.Return;
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
                // ActionDelay is mapped to Delay statement, and may not appear
                // in action visuals
                throw new IllegalStateException(
                        Statement.ActionDelay.toString());
            } else {
                visual = null;
            }
            // Message or txt?
            final boolean hasMessageStatement = visuals
                    .containsKey(Statement.Message);
            final boolean hasTxt = visuals.containsKey(Statement.Txt);
            if (hasMessageStatement && hasTxt)
                throw new ValidationError(
                        this,
                        "Spoken messages and .txt are exclusive because PCMPlayer/TeaseLib supports only one text area");
            final boolean hasMessage = hasMessageStatement || hasTxt;
            final boolean hasDelay;
            if (visual instanceof Delay) {
                hasDelay = ((Delay) visual).from > 0;
            } else {
                // Since the script ends with Quit, the delay is actually
                // infinite
                hasDelay = hasMessage;
            }
            if (hasDelay) {
                // Automatic mistress image
                if (!visuals.containsKey(Statement.Image)
                        && !visuals.containsKey(Statement.NoImage)) {
                    addVisual(Statement.Image, MistressImage.instance);
                }
                // Add empty message in order to render NoImage + NoMessage
                if (!hasMessage && !hasTxt) {
                    addVisual(Statement.Txt, NoMessage.instance);
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
                    throw new ValidationError(this,
                            "Without a message, a Delay statement is needed to display the image");
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

    /*
     * (non-Javadoc)
     * 
     * @see pcm.model.AbstractAction#add(pcm.model.ScriptLineTokenizer)
     */
    @Override
    public void add(ScriptLineTokenizer cmd) throws ParseError {
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
            // addTxt(cmd.all());
        } else if (name == Statement.Pause) {
            String resumeText = cmd.asText();
            if (!resumeText.isEmpty()) {
                addResponse(Statement.ResumeText, resumeText);
            }
            setInteraction(new Pause());
        } else if (name == Statement.Delay || name == Statement.ActionDelay) {
            String args[] = cmd.args();
            if (args.length == 1) {
                int delay = Integer.parseInt(args[0]);
                addVisual(Statement.Delay, new Delay(delay));
            } else if (args.length == 2) {
                int from = Integer.parseInt(args[0]);
                int to = Integer.parseInt(args[1]);
                addVisual(Statement.Delay, new Delay(from, to));
            } else if (args.length == 4) {
                int from = Integer.parseInt(args[0]);
                int to = Integer.parseInt(args[1]);
                addVisual(Statement.Delay, new Delay(from, to));
                int stop = Integer.parseInt(args[3]);
                setInteraction(new Stop(new ActionRange(stop)));
            } else if (args.length == 5) {
                int from = Integer.parseInt(args[0]);
                int to = Integer.parseInt(args[1]);
                addVisual(Statement.Delay, new Delay(from, to));
                int stopFrom = Integer.parseInt(args[3]);
                int stopTo = Integer.parseInt(args[4]);
                setInteraction(new Stop(new ActionRange(stopFrom, stopTo)));
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
        } else if (name == Statement.GoSub) {
            String args[] = cmd.args();
            int start = Integer.parseInt(args[0]);
            if (args.length > 1) {
                int end = Integer.parseInt(args[1]);
                setInteraction(new GoSub(new ActionRange(start, end)));
            } else {
                setInteraction(new GoSub(new ActionRange(start)));
            }
        } else if (name == Statement.Return) {
            setInteraction(new Return());
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
            // delay 0 & noimage
            if (visuals.containsKey(Statement.Delay)
                    && visuals.containsKey(Statement.NoImage)) {
                Delay delay = (Delay) visuals.get(Statement.Delay);
                if (delay.to == 0) {
                    validationErrors
                            .add(new ValidationError(this,
                                    "Delay 0 + NoImage is deprecated and should be removed"));
                }
            } else if (visuals.containsKey(Statement.Delay)) {
                Delay delay = (Delay) visuals.get(Statement.Delay);
                if (delay.to == 0) {
                    validationErrors.add(new ValidationError(this,
                            "Delay 0 is deprecated and should be removed"));
                }
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
