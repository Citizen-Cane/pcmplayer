package pcm;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import pcm.controller.Declarations;
import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ConditionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.StatementConditionRange;
import pcm.model.ValidationIssue;
import pcm.state.Condition;
import pcm.state.StateCommandLineParameters;
import pcm.state.conditions.StateCondition;
import pcm.util.TestUtils;
import teaselib.Body;
import teaselib.Posture;

public class ConditionRangeTestWithStateConditions {

    @Test
    public void testConditionRangeIsCaseIndependent() {
        Declarations declarations = new Declarations();
        declarations.add("teaselib.Toys", Declarations.STRING);
        declarations.add("teaselib.Toys", Declarations.STATE);
        declarations.add("teaselib.Body", Declarations.STRING);
        declarations.add("teaselib.Body", Declarations.STATE);

        Condition condition = new StateCondition(new StateCommandLineParameters(
                new String[] { "teaselib.Toys.Chastity_Device", "applied" }, declarations));
        Condition conditionWithDifferentCase = new StateCondition(new StateCommandLineParameters(
                new String[] { "teaseliB.toyS.chastitY_devicE", "aPPlied" }, declarations));

        Condition anotherCondition = new StateCondition(new StateCommandLineParameters(
                new String[] { "teaseliB.toyS.Something_Else", "applied" }, declarations));

        assertEquals(condition, conditionWithDifferentCase);

        ConditionRange cr = new StatementConditionRange(condition);
        ConditionRange crWithDifferentCase = new StatementConditionRange(conditionWithDifferentCase);

        assertTrue(cr.contains(condition));
        assertTrue(cr.contains(conditionWithDifferentCase));
        assertTrue(crWithDifferentCase.contains(condition));
        assertTrue(crWithDifferentCase.contains(conditionWithDifferentCase));

        assertFalse(cr.contains(anotherCondition));
        assertFalse(cr.contains(anotherCondition));
        assertFalse(crWithDifferentCase.contains(anotherCondition));
        assertFalse(crWithDifferentCase.contains(anotherCondition));

        ConditionRange crOther = new StatementConditionRange(anotherCondition);
        assertFalse(crOther.contains(condition));
        assertFalse(crOther.contains(conditionWithDifferentCase));
    }

    @Test
    public void testRemoveAllAtOnceWithActionNumbers()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        player.state(Body.OnPenis).applyTo();
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(20);
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());
        player.state(Body.InMouth).applyTo();
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

        player.state(Body.OnPenis).applyTo();
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state.set(400);
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
        player.state.set(401);
        assertEquals(1, player.range(new ActionRange(1400, 1402)).size());
        player.state.set(402);

        // condition relaxed by condition range declaration
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());
        player.state(Posture.CantKneel).applyTo();
        assertEquals(1, player.range(new ActionRange(1400, 1402)).size());
        assertEquals(1401, player.range(new ActionRange(1400, 1402)).get(0).number);
    }

    @Test
    public void testRemoveOneAfterAnotherConditionRangeOrderWithActionNumbers()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        Player player = TestUtils.createPlayer(getClass());
        player.loadScript(getClass().getSimpleName());

        player.state(Body.OnPenis).applyTo();
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state(Body.AroundNeck).applyTo();
        assertEquals(2, player.range(new ActionRange(1400, 1402)).size());
        assertNotEquals(1401, player.range(new ActionRange(1400, 1402)).get(0).number);
        assertNotEquals(1401, player.range(new ActionRange(1400, 1402)).get(1).number);

        player.state(Posture.CantKneel).applyTo();
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

        player.state(Body.OnPenis).applyTo();
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state(Body.AroundNeck).applyTo();
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

        player.state(Body.OnPenis).applyTo();
        assertEquals(3, player.range(new ActionRange(1400, 1402)).size());

        player.state(Body.AroundNeck).applyTo();
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
