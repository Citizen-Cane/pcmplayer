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

public class MappedScriptStateTest {
    private static final String MAPPED_STATE_TEST_SCRIPT = "MappedScriptStateTest";

    static final int SomethingOnPenisAction = 23;
    static final int ChastityCageAction = 44;

    private Player player;
    private State chastityCageState;
    private MappedScriptState pcm;

    enum Body {
        SomethingOnPenis,
        CannotJerkOff
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
        chastityCageState = player.state(Toys.Chastity_Cage);
        pcm = player.state;
    }

    private void installMapping() {
        pcm.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptStateValue.Indefinitely(ChastityCageAction,
                        chastityCageState, Body.SomethingOnPenis,
                        Body.CannotJerkOff));

        pcm.addStateTimeMapping(MappedScriptState.Global, ChastityCageAction,
                chastityCageState, Body.SomethingOnPenis, Body.CannotJerkOff);
    }

    private void loadTestScript() throws ScriptParsingException,
            ValidationIssue, IOException, ScriptExecutionException {
        player.loadScript(MAPPED_STATE_TEST_SCRIPT);
    }

    @Test
    public void testThatUninitializedMultiMappedStateCanBeRead()
            throws AllActionsSetException, ScriptExecutionException {
        assertThatUninitializedStateHasCorrectDefaultValues();

        TestUtils.play(player, 1000);

        assertEquals(ScriptState.SET, pcm.get(1000));
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

        chastityCageState.remove();
        assertEquals(UNSET, pcm.get(ChastityCageAction));
    }

    @Test
    public void assertThatUninitializedMultiMappedStateDefaultsToRemove() {
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        assertFalse(chastityCageState.applied());
        assertTrue(chastityCageState.peers().isEmpty());
        assertEquals(State.REMOVED,
                pcm.getTime(ChastityCageAction).getTime() / 1000);
    }

    @Test
    public void assertThatMultiMappedSetTimeSetsFlag() {
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        // TODO This may fail due to timing issues -> manually advance getTime()
        int expectedSeconds = 10;
        pcm.setTime(ChastityCageAction,
                player.teaseLib.getTime(TimeUnit.MILLISECONDS),
                1000 * expectedSeconds);
        assertEquals(expectedSeconds,
                chastityCageState.duration().remaining(TimeUnit.SECONDS));
        assertTrue(chastityCageState.applied());
        assertTrue(chastityCageState.peers().contains(Body.SomethingOnPenis));
        assertTrue(chastityCageState.peers().contains(Body.CannotJerkOff));
        assertEquals(SET, pcm.get(ChastityCageAction));
    }

    @Test
    public void assertThatUnsetFlagResetsStateTime() {
        pcm.unset(ChastityCageAction);
        assertFalse(chastityCageState.applied());
        assertEquals(State.REMOVED,
                chastityCageState.duration().limit(TimeUnit.SECONDS));
        assertEquals(State.REMOVED,
                pcm.getTime(ChastityCageAction).getTime() / 1000
                        - player.teaseLib.getTime(TimeUnit.SECONDS));
    }

    @Test
    public void assertThatSetMultiMappedFlagSetsTime() {
        pcm.set(ChastityCageAction);
        assertTrue(chastityCageState.applied());
        assertTrue(chastityCageState.peers().contains(Body.SomethingOnPenis));
        assertTrue(chastityCageState.peers().contains(Body.CannotJerkOff));
        assertTrue(chastityCageState.expired());

        long time = pcm.getTime(ChastityCageAction).getTime();
        assertEquals(player.teaseLib.getTime(TimeUnit.MILLISECONDS), time,
                1000);
    }

    @Test
    public void testThatUnmappedScriptTimeValuesCorrectly()
            throws AllActionsSetException, ScriptExecutionException,
            ScriptParsingException, ValidationIssue, IOException {
        Player testScript = TestUtils.createPlayer(getClass(),
                MAPPED_STATE_TEST_SCRIPT);

        TestUtils.play(testScript, 1025);
        TestUtils.play(testScript, 1039);

        assertEquals(ScriptState.SET, testScript.state.get(1025));
        assertThatScriptTimeFuctionsWork(testScript.state);
        assertEquals(ScriptState.SET, testScript.state.get(1031));
        assertEquals(ScriptState.SET, testScript.state.get(1039));
    }

    @Test
    public void testThatScriptHandlesMultiMappedTimeValuesCorrectly()
            throws AllActionsSetException, ScriptExecutionException {
        assertThatUninitializedStateHasCorrectDefaultValues();

        TestUtils.play(player, 1020);
        TestUtils.play(player, 1038);

        assertEquals(ScriptState.SET, pcm.get(1020));
        assertEquals(ScriptState.SET, pcm.get(1022));
        assertThatScriptTimeFuctionsWork(pcm);
        assertEquals(ScriptState.SET, pcm.get(1030));
        assertEquals(ScriptState.SET, pcm.get(1038));

        assertThatSettingTheMappedActionSetsTheStateToTemporary();
    }

    private void assertThatSettingTheMappedActionSetsTheStateToTemporary() {
        assertEquals(State.TEMPORARY,
                chastityCageState.duration().limit(TimeUnit.SECONDS));
    }

    private void assertThatUninitializedStateHasCorrectDefaultValues() {
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        assertFalse(chastityCageState.applied());
        assertTrue(chastityCageState.peers().isEmpty());

        long seconds = pcm.getTime(ChastityCageAction).getTime() / 1000;
        assertEquals(State.REMOVED, seconds);
    }

    private static void assertThatScriptTimeFuctionsWork(
            ScriptState scriptState) {
        assertEquals(ScriptState.SET, scriptState.get(1025));
        assertEquals(ScriptState.SET, scriptState.get(1027));
        assertEquals(ScriptState.SET, scriptState.get(1029));

    }

    @Test
    public void testThatStatesWithMultiplePeersWorkAsExpectedEvenWithSelfReferencingItems()
            throws AllActionsSetException, ScriptExecutionException,
            ScriptParsingException, ValidationIssue, IOException {
        assertThatUninitializedStateHasCorrectDefaultValues();

        State somethingOnPenisState = player.state(Body.SomethingOnPenis);
        pcm.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptStateValue.Indefinitely(SomethingOnPenisAction,
                        somethingOnPenisState, Body.SomethingOnPenis));

        pcm.addStateTimeMapping(MappedScriptState.Global,
                SomethingOnPenisAction, somethingOnPenisState,
                Body.SomethingOnPenis);

        loadTestScript();

        TestUtils.play(player, 1050);
    }
}
