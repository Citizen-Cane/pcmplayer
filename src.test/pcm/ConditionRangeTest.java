package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.junit.Test;

import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestPlayer;

public class ConditionRangeTest {

    @Test
    public void testDefaultConditionRange()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = new TestPlayer(getClass());
        player.loadScript(getClass().getSimpleName() + "ConditionsWithDefault");

        player.play(1000);
        assertEquals(ScriptState.SET, player.state.get(1000));

        player.play(new ActionRange(1001, 1002));
        assertEquals(ScriptState.UNSET, player.state.get(1001));
        assertEquals(ScriptState.SET, player.state.get(1002));
    }

    @Test
    public void testRemoveAllAtOnceWithActionNumbers()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(20);
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());
        player.state.set(22);
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());
        player.state.set(25);
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());
        player.state.set(39);
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(32);
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
        assertEquals(1400, player.range(new ActionRange(1400, 1402)).get(0).number);
        assertEquals(1402, player.range(new ActionRange(1400, 1402)).get(1).number);
    }

    @Test
    public void testRemoveOneAfterAnotherWithActionNumbers()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(400);
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
        player.state.set(401);
        assertEquals(1, player.range(new ActionRange(1400, 1402)).size());
        player.state.set(402);

        // condition relaxed by condition range declaration
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());
        player.state.set(33);
        assertEquals(1, player.range(new ActionRange(1400, 1402)).size());
        assertEquals(1401, player.range(new ActionRange(1400, 1402)).get(0).number);
    }

    @Test
    public void testRemoveOneAfterAnotherConditionRangeOrderWithActionNumbers()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(28);
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
        assertNotEquals(1401, player.range(new ActionRange(1400, 1402)).get(0).number);
        assertNotEquals(1401, player.range(new ActionRange(1400, 1402)).get(1).number);

        player.state.set(33);
        // 1401 is first block by .shouldnot 28
        // but 1400 and 1402 are blocked by 33
        // -> condition ranges are removed until .shouldnot 28 is removed
        // As a result, 1401 is available
        assertEquals(1, player.range(new ActionRange(1400, 1402)).size());
        assertEquals(1401, player.range(new ActionRange(1400, 1402)).get(0).number);
    }

    @Test
    public void testRemoveOneAfterAnotherConditionRangeOrderWithActionNumbers2()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(28);
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(400);
        player.state.set(401);
        player.state.set(402);

        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
    }

    @Test
    public void demonstrateOrderingByRelaxingRangesBelowAndAbove()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        TestPlayer player = TestPlayer.loadScript(getClass());

        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(28);
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());

        // condition range below 28
        player.state.set(20);
        // also relaxes the collar since conditions must be relaxed until
        // .shouldnot 20 is relaxed
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());
        player.state.unset(20);

        // condition range above 28
        player.state.set(39);
        // Doesn't relax the collar because .shouldnot 39 is relaxed before
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
        player.state.unset(39);

        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
    }
}
