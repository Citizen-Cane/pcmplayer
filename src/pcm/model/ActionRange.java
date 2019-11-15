package pcm.model;

import static java.lang.Integer.parseInt;

import java.util.Optional;

import teaselib.util.Interval;

public class ActionRange extends Interval implements ConditionRange {

    public static ActionRange of(String... args) {
        int start = parseInt(args[0]);
        if (args.length > 1) {
            int end = parseInt(args[1]);
            return ActionRange.of(start, end);
        } else {
            return ActionRange.of(start);
        }
    }

    public static ActionRange of(int start, int end) {
        return new ActionRange(start, end);
    }

    public static ActionRange of(int start) {
        return new ActionRange(start, start);
    }

    public ActionRange(int start) {
        super(start, start);
    }

    public ActionRange(int start, int end) {
        super(start, end);
    }

    public Optional<String> script() {
        return Optional.empty();
    }

    public boolean validate() {
        return start <= end;
    }

    public boolean isInside(ConditionRange conditionRange) {
        return conditionRange.contains(start) && conditionRange.contains(end);
    }

    @Override
    public boolean contains(Object n) {
        return n instanceof Integer && super.contains((Integer) n);
    }

    @Override
    public String toString() {
        if (start == end) {
            return Integer.toString(start);
        } else {
            return start + "-" + end;
        }
    }
}
