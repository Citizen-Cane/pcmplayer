/**
 * 
 */
package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestPlayer;

/**
 * @author Citizen-Cane
 *
 */
public class ScriptTests {
    final Player player;

    public ScriptTests() throws IOException, ValidationIssue, ScriptExecutionException, ScriptParsingException {
        player = TestPlayer.loadScript(getClass());
    }

    @Test
    public void testAllUnsetAfterInit() throws ScriptExecutionException {
        assertEquals(0, player.state.size());

        player.play(new ActionRange(9999), new ActionRange(9999));

        assertEquals(ScriptState.SET, player.state.get(9999));
        assertEquals(1, player.state.size());
    }

    @Test
    public void testOutOfActions() throws ScriptExecutionException {
        ActionRange r = new ActionRange(1000, 1001);
        player.state.set(9);
        player.playOnly(r);
        assertEquals(ScriptState.SET, player.state.get(1000));
        assertEquals(ScriptState.UNSET, player.state.get(1001));
        assertEquals(9999, player.action.number);
        try {
            player.playOnly(r);
            assertTrue("Unexpected action available", false);
        } catch (AllActionsSetException e) {
            assertEquals(ScriptState.SET, player.state.get(9));
            assertEquals(ScriptState.SET, player.state.get(1000));
            assertEquals(ScriptState.UNSET, player.state.get(1001));
        }
    }

    // Logged repeatAdd/Del, added test case to document the feature
    @Test
    public void testRepeatAdd() throws ScriptExecutionException {
        ActionRange r = new ActionRange(1010, 1011);
        assertEquals(ScriptState.UNSET, player.state.get(1010));
        player.playOnly(r);
        assertEquals(ScriptState.SET, player.state.get(1010));
        // unset plus repeatAdd 1001 2 -> 3 times
        assertEquals(-2, player.state.get(1011).intValue());
        player.playOnly(r);
        assertEquals(-1, player.state.get(1011).intValue());
        player.playOnly(r);
        assertEquals(-0, player.state.get(1011).intValue());
        assertEquals(ScriptState.UNSET, player.state.get(1011));
        player.playOnly(r);
        assertTrue(player.state.get(1011) == ScriptState.SET);
    }

    @Test
    public void testRepeatDel() throws ScriptExecutionException {
        ActionRange r = new ActionRange(1020, 1023);
        assertEquals(ScriptState.UNSET, player.state.get(1020));
        player.playOnly(r);
        assertEquals(ScriptState.SET, player.state.get(1020));
        // unset plus repeatAdd 1001 2 -> 3 times
        assertEquals(ScriptState.SET, player.state.get(1021));
        assertEquals(ScriptState.SET, player.state.get(1022));
        assertEquals(-3, player.state.get(1023).intValue());
        player.playOnly(r);
        assertEquals(-1, player.state.get(1023).intValue());
        player.playOnly(r);
        assertEquals(ScriptState.SET, player.state.get(1023));
    }

    @Test
    public void testShouldnotWithDefaultConditionRange() throws ScriptExecutionException {
        ActionRange r = new ActionRange(1030, 1030);
        assertEquals(ScriptState.UNSET, player.state.get(1030));
        player.playOnly(r);
        assertEquals(ScriptState.SET, player.state.get(1030));

        r = new ActionRange(1031, 1032);
        player.playOnly(r);

        assertEquals(ScriptState.UNSET, player.state.get(1031));
        assertEquals(ScriptState.SET, player.state.get(1032));
    }

    @Test
    public void testResetRange() throws ScriptExecutionException {
        player.play(new ActionRange(1040), new ActionRange(1040, 1041));

        assertEquals(ScriptState.UNSET, player.state.get(1040));
        assertEquals(ScriptState.UNSET, player.state.get(1041));
        assertEquals(ScriptState.UNSET, player.state.get(1042));
        assertEquals(ScriptState.UNSET, player.state.get(1043));

        assertThrows(IllegalArgumentException.class, () -> player.state.getTime(1043));
    }

    @Test
    public void testMustNotAllOf() throws ScriptExecutionException {
        player.play(new ActionRange(1050), new ActionRange(1050, 1059));

        assertEquals(ScriptState.SET, player.state.get(1053));
        assertEquals(ScriptState.SET, player.state.get(1054));
        assertEquals(ScriptState.SET, player.state.get(1058));
    }

    @Test
    public void testMustAnyOf() throws ScriptExecutionException {
        player.play(new ActionRange(1060), new ActionRange(1060, 1069));

        assertEquals(ScriptState.SET, player.state.get(1062));
        assertEquals(ScriptState.SET, player.state.get(1064));
        assertEquals(ScriptState.SET, player.state.get(1068));
    }
}
