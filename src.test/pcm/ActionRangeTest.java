package pcm;

import org.junit.jupiter.api.Test;

import pcm.model.ActionRange;
import teaselib.util.Interval;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionRangeTest {
    final int start = 100;
    final int end = 200;

    @Test
    public void testActionRange() {
        Interval r = new ActionRange(start, end);
        assertFalse(r.contains(start - 1));
        assertTrue(r.contains(start));
        assertTrue(r.contains(start + 1));

        assertTrue(r.contains(end - 1));
        assertTrue(r.contains(end));
        assertFalse(r.contains(end + 1));
    }

    @Test
    public void testSingularActionRange() {
        Interval r = new ActionRange(start);
        assertFalse(r.contains(start - 1));
        assertTrue(r.contains(start));
        assertFalse(r.contains(start + 1));
    }

}
