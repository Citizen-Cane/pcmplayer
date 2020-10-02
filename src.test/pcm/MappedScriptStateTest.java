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
import teaselib.Household;
import teaselib.State;
import teaselib.core.Debugger;
import teaselib.core.state.StateProxy;

public class MappedScriptStateTest {
    private static final String MAPPED_STATE_TEST_SCRIPT = "MappedScriptStateTest";

    static final int SomethingOnPenisAction = 23;
    static final int ChastityCageAction = 44;
    static final int CondomsAction = 46;

    private Player player;
    private Debugger debugger;
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
    public void before() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        initPlayer();
        installMapping();
        loadTestScript();
    }

    private void initPlayer() throws IOException {
        player = TestUtils.createPlayer(getClass());
        debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        chastityCageState = player.state(Toys.Chastity_Cage);
        pcm = player.state;
    }

    private void installMapping() {
        pcm.addScriptValueMapping(MappedScriptState.Global, new MappedScriptStateValue.ForSession(ChastityCageAction,
                chastityCageState, Body.SomethingOnPenis, Body.CannotJerkOff));

        pcm.addStateTimeMapping(MappedScriptState.Global, ChastityCageAction, chastityCageState, Body.SomethingOnPenis,
                Body.CannotJerkOff);
    }

    private void loadTestScript()
            throws ScriptParsingException, ValidationIssue, IOException, ScriptExecutionException {
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
    public void testThatUninitializedUnmappedStateThrows() throws AllActionsSetException, ScriptExecutionException {
        TestUtils.play(player, 1010);

        assertEquals(SET, pcm.get(1010));
        assertEquals(SET, pcm.get(9999));
    }

    @Test
    public void testThatMultiMappedValuesBehaveAsExpected() {
        assertThatUninitializedMultiMappedStateDefaultsToTemporary();
        assertThatMultiMappedSetTimeSetsFlag();
        assertThatUnsetFlagDoesntResetDuration(10);
        long chastityActionRemoveTime = pcm.getTime(ChastityCageAction);
        // pcm.getTime() gets duration end
        assertEquals(player.state(Toys.Chastity_Cage).duration().start(TimeUnit.SECONDS), chastityActionRemoveTime);
        assertEquals(player.teaseLib.getTime(TimeUnit.SECONDS), chastityActionRemoveTime);
        assertThatSetMultiMappedFlagSetsStateToAppliedTemporary();

        chastityCageState.remove();
        assertEquals(UNSET, pcm.get(ChastityCageAction));
    }

    @Test
    public void assertThatUninitializedMultiMappedStateDefaultsToTemporary() {
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        assertFalse(chastityCageState.applied());
        assertTrue(chastityCageState.expired());
        assertTrue(((StateProxy) chastityCageState).peers().isEmpty());
        assertEquals(0, pcm.getTime(ChastityCageAction));
    }

    @Test
    public void assertThatMultiMappedSetTimeSetsFlag() {
        assertEquals(UNSET, pcm.get(ChastityCageAction));

        pcm.setTime(ChastityCageAction, player.duration(10, TimeUnit.SECONDS));
        assertEquals(10, chastityCageState.duration().remaining(TimeUnit.SECONDS));
        assertTrue(chastityCageState.applied());
        assertFalse(chastityCageState.expired());
        assertTrue(chastityCageState.is(Body.SomethingOnPenis));
        assertTrue(chastityCageState.is(Body.CannotJerkOff));
        assertEquals(SET, pcm.get(ChastityCageAction));

        debugger.advanceTime(10, TimeUnit.SECONDS);

        assertTrue(chastityCageState.applied());
        assertTrue(chastityCageState.expired());

        assertEquals(SET, pcm.get(ChastityCageAction));

        assertEquals(0, chastityCageState.duration().remaining(TimeUnit.SECONDS));
    }

    @Test
    public void assertThatUnsetFlagDoesntResetDuration() {
        assertThatUnsetFlagDoesntResetDuration(State.TEMPORARY);
    }

    public void assertThatUnsetFlagDoesntResetDuration(long duration) {
        pcm.unset(ChastityCageAction);
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        assertFalse(chastityCageState.applied());
        assertTrue(chastityCageState.expired());
        assertEquals(duration, chastityCageState.duration().limit(TimeUnit.SECONDS));

        // Mapping isn't allowed anymore since TeaseLib doesn't allow infinite states anymore
        pcm.set(ChastityCageAction);
        long time = player.teaseLib.getTime(TimeUnit.SECONDS);
        assertEquals(time, chastityCageState.duration().start(TimeUnit.SECONDS));
    }

    @Test
    public void assertThatSetMultiMappedFlagSetsStateToAppliedTemporary() {
        pcm.set(ChastityCageAction);
        assertTrue(chastityCageState.applied());
        assertTrue(chastityCageState.expired());

        assertTrue(chastityCageState.is(Body.SomethingOnPenis));
        assertTrue(chastityCageState.is(Body.CannotJerkOff));

        long time = player.teaseLib.getTime(TimeUnit.SECONDS);
        long durationEnd = pcm.getTime(ChastityCageAction);
        assertEquals(time, durationEnd);
    }

    @Test
    public void testThatScriptHandlesUnmappedTimeValuesCorrectly() throws AllActionsSetException,
            ScriptExecutionException, ScriptParsingException, ValidationIssue, IOException {
        Player testScript = TestUtils.createPlayer(getClass(), MAPPED_STATE_TEST_SCRIPT);
        Debugger debugger = new Debugger(testScript.teaseLib);
        debugger.freezeTime();

        TestUtils.play(testScript, 1025);
        TestUtils.play(testScript, 1038);

        assertEquals(ScriptState.SET, testScript.state.get(1025));
        assertThatScriptTimeFuctionsWork(testScript.state);
        assertEquals(ScriptState.SET, testScript.state.get(1031));
        assertEquals(ScriptState.SET, testScript.state.get(1038));
    }

    @Test
    public void testThatSettimeOnMultiMappedTimeValuesAppliesToPeers()
            throws AllActionsSetException, ScriptExecutionException {
        assertThatUninitializedStateHasCorrectDefaultValues();

        TestUtils.play(player, 1034);
        TestUtils.play(player, 1037);
        TestUtils.play(player, 1039);
        assertEquals(ScriptState.SET, pcm.get(1034));
        assertEquals(ScriptState.SET, pcm.get(1036));
        assertEquals(ScriptState.SET, pcm.get(1037));
        assertEquals(ScriptState.SET, pcm.get(1039));

        assertThatSettingTheMappedActionSetsTheStateToTemporary();
    }

    @Test
    public void testThatScriptHandlesMultiMappedTimeValuesCorrectly()
            throws AllActionsSetException, ScriptExecutionException {
        assertThatUninitializedStateHasCorrectDefaultValues();

        TestUtils.play(player, 1020);
        assertEquals(ScriptState.SET, pcm.get(1020));
        assertEquals(ScriptState.SET, pcm.get(1022));
        assertThatScriptTimeFuctionsWork(pcm);
        assertEquals(ScriptState.SET, pcm.get(1030));
        assertEquals(ScriptState.SET, pcm.get(1036));

        TestUtils.play(player, 1037);
        TestUtils.play(player, 1039);

        assertEquals(ScriptState.SET, pcm.get(1037));
        assertEquals(ScriptState.SET, pcm.get(1039));

        assertThatSettingTheMappedActionSetsTheStateToTemporary();
    }

    private void assertThatSettingTheMappedActionSetsTheStateToTemporary() {
        assertEquals(State.TEMPORARY, chastityCageState.duration().limit(TimeUnit.SECONDS));
    }

    private void assertThatUninitializedStateHasCorrectDefaultValues() {
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        assertFalse(chastityCageState.applied());
        assertTrue(chastityCageState.expired());
        assertTrue(((StateProxy) chastityCageState).peers().isEmpty());

        long seconds = pcm.getTime(ChastityCageAction);
        assertEquals(State.TEMPORARY, seconds);
    }

    private static void assertThatScriptTimeFuctionsWork(ScriptState scriptState) {
        assertEquals(ScriptState.SET, scriptState.get(1025));
        assertEquals(ScriptState.SET, scriptState.get(1027));
        assertEquals(ScriptState.SET, scriptState.get(1029));
    }

    @Test
    public void testThatStatesWithMultiplePeersWorkAsExpectedWithSelfReferencingItems() throws AllActionsSetException,
            ScriptExecutionException, ScriptParsingException, ValidationIssue, IOException {
        assertThatUninitializedStateHasCorrectDefaultValues();

        State condomState = player.state(Household.Condoms);
        pcm.addScriptValueMapping(MappedScriptState.Global, new MappedScriptStateValue.ForSession(CondomsAction,
                condomState, Household.Condoms, Body.SomethingOnPenis));

        pcm.addStateTimeMapping(MappedScriptState.Global, CondomsAction, condomState, Household.Condoms,
                Body.SomethingOnPenis);

        pcm.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptStateValue.ForSession(SomethingOnPenisAction, condomState, Body.SomethingOnPenis));

        loadTestScript();

        TestUtils.play(player, 1050);
    }

    @Test
    public void testThatStatesWithMultiplePeersWorkAsExpectedWithSelfReferencingItemsInJava()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        assertThatUninitializedStateHasCorrectDefaultValues();

        State condomState = player.state(Household.Condoms);
        pcm.addScriptValueMapping(MappedScriptState.Global, new MappedScriptStateValue.ForSession(CondomsAction,
                condomState, Household.Condoms, Body.SomethingOnPenis));

        pcm.addStateTimeMapping(MappedScriptState.Global, CondomsAction, condomState, Household.Condoms,
                Body.SomethingOnPenis);

        pcm.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptStateValue.ForSession(SomethingOnPenisAction, condomState, Body.SomethingOnPenis));

        // Load test script to actually select the mapping
        loadTestScript();

        pcm.set(CondomsAction);
        pcm.set(ChastityCageAction);

        assertEquals(SET, pcm.get(SomethingOnPenisAction));
        assertEquals(SET, pcm.get(ChastityCageAction));
        assertEquals(SET, pcm.get(CondomsAction));

        pcm.unset(ChastityCageAction);
        assertEquals(SET, pcm.get(SomethingOnPenisAction));
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        assertEquals(SET, pcm.get(CondomsAction));

        pcm.unset(CondomsAction);
        assertEquals(UNSET, pcm.get(SomethingOnPenisAction));
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        assertEquals(UNSET, pcm.get(CondomsAction));
    }

    @Test
    public void testThatScriptHandlesAppliedAndExpiredCorrectly()
            throws AllActionsSetException, ScriptExecutionException {
        assertThatUninitializedStateHasCorrectDefaultValues();

        TestUtils.play(player, 1060);
        assertTrue(chastityCageState.applied());
        assertFalse(chastityCageState.expired());

        debugger.advanceTime(10, TimeUnit.SECONDS);

        TestUtils.play(player, 1062);
        assertTrue(chastityCageState.applied());
        assertTrue(chastityCageState.expired());

        TestUtils.play(player, 1063);
        assertFalse(chastityCageState.applied());
        assertTrue(chastityCageState.expired());
    }
}
