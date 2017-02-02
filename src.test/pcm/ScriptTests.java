/**
 * 
 */
package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestUtils;
import teaselib.Actor;
import teaselib.core.ResourceLoader;
import teaselib.core.TeaseLib;
import teaselib.core.texttospeech.Voice;
import teaselib.hosts.DummyHost;
import teaselib.hosts.DummyPersistence;

/**
 * @author someone
 *
 */
public class ScriptTests {

    Player player = new Player(
            new TeaseLib(new DummyHost(), new DummyPersistence()),
            new ResourceLoader(ScriptTests.class),
            new Actor(Actor.Key.DominantFemale, Voice.Gender.Female, Locale.US),
            "pcm", null) {

        @Override
        public void run() {
        }
    };

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUpBefore() throws Exception {
        player.loadScript("ScriptTests");
    }

    @Test
    public void testOutOfActions() throws Exception {
        ActionRange r = new ActionRange(1000, 1001);
        player.range = r;
        player.state.set(9);
        player.play(r);
        assertEquals(ScriptState.SET, player.state.get(1000));
        assertEquals(ScriptState.UNSET, player.state.get(1001));
        assertEquals(9999, player.range.start);
        try {
            player.range = r;
            player.play(r);
            assertTrue("Unexpected action available", false);
        } catch (AllActionsSetException e) {
            assertEquals(ScriptState.SET, player.state.get(9));
            assertEquals(ScriptState.SET, player.state.get(1000));
            assertEquals(ScriptState.UNSET, player.state.get(1001));
        }
    }

    // Logged repeatAdd/Del, added test case to document the feature
    @Test
    public void testRepeatAdd() throws Exception {
        ActionRange r = new ActionRange(1010, 1011);
        assertEquals(ScriptState.UNSET, player.state.get(1010));
        player.range = r;
        player.play(r);
        assertEquals(ScriptState.SET, player.state.get(1010));
        // unset plus repeatAdd 1001 2 -> 3 times
        assertEquals(-2, player.state.get(1011).intValue());
        player.range = r;
        player.play(r);
        assertEquals(-1, player.state.get(1011).intValue());
        player.range = r;
        player.play(r);
        assertEquals(-0, player.state.get(1011).intValue());
        assertEquals(ScriptState.UNSET, player.state.get(1011));
        player.range = r;
        player.play(r);
        assertTrue(player.state.get(1011) == ScriptState.SET);
    }

    @Test
    public void testRepeatDel() throws Exception {
        ActionRange r = new ActionRange(1020, 1023);
        assertEquals(ScriptState.UNSET, player.state.get(1020));
        player.range = r;
        player.play(r);
        assertEquals(ScriptState.SET, player.state.get(1020));
        // unset plus repeatAdd 1001 2 -> 3 times
        assertEquals(ScriptState.SET, player.state.get(1021));
        assertEquals(ScriptState.SET, player.state.get(1022));
        assertEquals(-3, player.state.get(1023).intValue());
        player.range = r;
        player.play(r);
        assertEquals(-1, player.state.get(1023).intValue());
        player.range = r;
        player.play(r);
        assertEquals(ScriptState.SET, player.state.get(1023));
    }

    @Test
    public void testShouldnotWithDefaultConditionRange() throws Exception {
        ActionRange r = new ActionRange(1030, 1030);
        assertEquals(ScriptState.UNSET, player.state.get(1030));
        player.range = r;
        player.play(r);
        assertEquals(ScriptState.SET, player.state.get(1030));

        r = new ActionRange(1031, 1032);
        player.range = r;
        player.play(r);

        assertEquals(ScriptState.SET, player.state.get(1032));
    }

    @Test
    public void testResetRange()
            throws AllActionsSetException, ScriptExecutionException,
            ScriptParsingException, ValidationIssue, IOException {
        Player player = TestUtils.createPlayer(getClass(), "ScriptTests");
        TestUtils.play(player, new ActionRange(1040),
                new ActionRange(1040, 1041));

        assertEquals(ScriptState.UNSET, player.state.get(1040));
        assertEquals(ScriptState.UNSET, player.state.get(1041));
        assertEquals(ScriptState.UNSET, player.state.get(1042));
        assertEquals(ScriptState.UNSET, player.state.get(1043));
        assertNull(player.state.getTime(1043));
    }
}
