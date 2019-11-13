package pcm.model;

import teaselib.util.Interval;

public class ActionRange extends Interval implements ConditionRange {

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
            return "Range " + start;
        } else {
            return "Range " + start + "-" + end;
        }
    }
}
