package pcm;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.util.TestUtils;
import teaselib.Body;

public class ConditionRangeTestWithStateConditions {

    @Test
    public void testRemoveAllAtOnceWithActionNumbers()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        player.state(Body.OnPenis).apply();
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(20);
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());
        player.state(Body.InMouth).apply();
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
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        player.state(Body.OnPenis).apply();
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(400);
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
        player.state.set(401);
        assertEquals(1, player.range(new ActionRange(1400, 1402)).size());
        player.state.set(402);

        // condition relaxed by condition range declaration
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());
        player.state(Body.CantKneel).apply();
        assertEquals(1, player.range(new ActionRange(1400, 1402)).size());
        assertEquals(1401, player.range(new ActionRange(1400, 1402)).get(0).number);
    }

    @Test
    public void testRemoveOneAfterAnotherConditionRangeOrderWithActionNumbers()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        player.state(Body.OnPenis).apply();
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state(Body.AroundNeck).apply();
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
        assertNotEquals(1401, player.range(new ActionRange(1400, 1402)).get(0).number);
        assertNotEquals(1401, player.range(new ActionRange(1400, 1402)).get(1).number);

        player.state(Body.CantKneel).apply();
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
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        player.state(Body.OnPenis).apply();
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state(Body.AroundNeck).apply();
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(400);
        player.state.set(401);
        player.state.set(402);

        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
    }

    @Test
    public void ensureCodeCoverageOfShould()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        assertEquals(1, player.range(new ActionRange(1400, 1402)).size());
        assertEquals(1400, player.range(new ActionRange(1400, 1402)).get(0).number);

        player.state(Body.OnPenis).apply();
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state(Body.AroundNeck).apply();
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
        assertEquals(1400, player.range(new ActionRange(1400, 1402)).get(0).number);
        assertEquals(1402, player.range(new ActionRange(1400, 1402)).get(1).number);

        player.state.set(400);
        player.state.set(401);
        player.state.set(402);

        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
        assertEquals(1400, player.range(new ActionRange(1400, 1402)).get(0).number);
        assertEquals(1402, player.range(new ActionRange(1400, 1402)).get(1).number);

        player.state(Body.OnPenis).remove();
        assertEquals(1, player.range(new ActionRange(1400, 1402)).size());
        assertEquals(1400, player.range(new ActionRange(1400, 1402)).get(0).number);
    }
}
