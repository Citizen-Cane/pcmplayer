package pcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static pcm.state.persistence.ScriptState.SET;
import static pcm.state.persistence.ScriptState.UNSET;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.persistence.MappedScriptState;
import pcm.state.persistence.MappedScriptStateValue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestUtils;
import teaselib.State;

public class MappedStateTest {
    static final int flag = 44;

    private Player player;
    private State state;
    private MappedScriptState pcm;

    enum Body {
        SomethingOnPenis
    }

    enum Toys {
        Chastity_Cage
    }

    @Before
    public void before() throws ScriptParsingException, ValidationIssue,
            ScriptExecutionException, IOException {
        initPlayer();
        installMapping();
        loadTestScript();
    }

    private void initPlayer() {
        player = TestUtils.createPlayer(getClass());
        state = player.state(Body.SomethingOnPenis);
        pcm = player.state;
    }

    private void installMapping() {
        pcm.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptStateValue.Indefinitely(flag, state,
                        Toys.Chastity_Cage));

        pcm.addStateTimeMapping(MappedScriptState.Global, flag, state,
                Toys.Chastity_Cage);
    }

    private void loadTestScript() throws ScriptParsingException,
            ValidationIssue, IOException, ScriptExecutionException {
        player.loadScript("MappedStateTest");
    }

    @Test
    public void testThatUninitializedMultiMappedStateCanBeRead()
            throws AllActionsSetException, ScriptExecutionException {
        assertThatUninitializedStateHasCorrectDefaultValues();

        TestUtils.play(player, 1000);

        assertEquals(ScriptState.SET, pcm.get(1000));
        assertEquals(ScriptState.SET, pcm.get(9999));
    }

    @Test(expected = java.lang.RuntimeException.class)
    public void testThatUninitializedUnmappedStateThrows()
            throws AllActionsSetException, ScriptExecutionException {
        TestUtils.play(player, 1010);

        assertEquals(SET, pcm.get(1010));
        assertEquals(SET, pcm.get(9999));
    }

    @Test
    public void testThatMultiMappedValuesBehaveAsExpected() {
        assertThatUninitializedMultiMappedStateDefaultsToRemove();
        assertThatMultiMappedSetTimeSetsFlag();
        assertThatUnsetFlagResetsStateTime();
        assertThatSetMultiMappedFlagSetsTime();

        state.remove();
        assertEquals(UNSET, pcm.get(flag));
    }

    private void assertThatUninitializedMultiMappedStateDefaultsToRemove() {
        assertEquals(UNSET, pcm.get(flag));
        assertFalse(state.applied());
        assertEquals(state.what(), State.None);
        assertEquals(State.REMOVED, pcm.getTime(flag).getTime() / 1000);
    }

    private void assertThatMultiMappedSetTimeSetsFlag() {
        assertEquals(UNSET, pcm.get(flag));
        // TODO This may fail due to timing issues -> manually advance getTime()
        int expectedSeconds = 10;
        pcm.setTime(flag, player.teaseLib.getTime(TimeUnit.MILLISECONDS),
                1000 * expectedSeconds);
        assertEquals(expectedSeconds, state.expected());
        assertEquals(expectedSeconds, state.remaining(TimeUnit.SECONDS));
        assertTrue(state.applied());
        assertEquals(state.what(), Toys.Chastity_Cage);
        assertEquals(SET, pcm.get(flag));
    }

    private void assertThatUnsetFlagResetsStateTime() {
        pcm.unset(flag);
        assertFalse(state.applied());
        assertEquals(state.what(), Toys.Chastity_Cage);
        assertEquals(State.REMOVED, state.expected());
        // TODO This may fail due to timing issues -> manually advance getTime()
        assertEquals(State.REMOVED, pcm.getTime(flag).getTime() / 1000
                - player.teaseLib.getTime(TimeUnit.SECONDS));
    }

    private void assertThatSetMultiMappedFlagSetsTime() {
        pcm.set(flag);
        assertTrue(state.applied());
        assertEquals(state.what(), Toys.Chastity_Cage);
        assertTrue(state.expired());

        long time = pcm.getTime(flag).getTime();
        // TODO This may fail due to timing issues -> manually advance getTime()
        assertEquals(player.teaseLib.getTime(TimeUnit.MILLISECONDS), time,
                1000);
    }

    @Test
    public void testThatUnmappedScriptTimeValuesCorrectly()
            throws AllActionsSetException, ScriptExecutionException,
            ScriptParsingException, ValidationIssue, IOException {
        initPlayer();
        loadTestScript();
        TestUtils.play(player, 1025);
        assertEquals(ScriptState.SET, pcm.get(1025));
        assertThatScriptTimeFuctionsWork();
    }

    @Test
    public void testThatScriptHandlesMultiMappedTimeValuesCorrectly()
            throws AllActionsSetException, ScriptExecutionException {
        assertThatUninitializedStateHasCorrectDefaultValues();
        TestUtils.play(player, 1020);

        assertEquals(ScriptState.SET, pcm.get(1020));
        assertEquals(ScriptState.SET, pcm.get(1022));
        assertThatScriptTimeFuctionsWork();

        assertEquals(600, state.expected());
    }

    private void assertThatUninitializedStateHasCorrectDefaultValues() {
        assertEquals(UNSET, pcm.get(flag));
        assertFalse(state.applied());
        assertEquals(state.what(), State.None);

        long seconds = pcm.getTime(flag).getTime() / 1000;
        assertEquals(State.REMOVED, seconds);
    }

    private void assertThatScriptTimeFuctionsWork() {
        assertEquals(ScriptState.SET, pcm.get(9999));

        assertEquals(ScriptState.SET, pcm.get(1025));
        assertEquals(ScriptState.SET, pcm.get(1027));
        assertEquals(ScriptState.SET, pcm.get(1029));

    }

}
