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
import teaselib.TeaseScriptPersistence;
import teaselib.core.Debugger;

public class MappedScriptStateTest {
    private static final String MAPPED_STATE_TEST_SCRIPT = "MappedScriptStateTest";

    static final int SomethingOnPenisAction = 23;
    static final int ChastityCageAction = 44;
    static final int CondomsAction = 46;

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
    public void before() throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        initPlayer();
        installMapping();
        loadTestScript();
    }

    private void initPlayer() throws IOException {
        player = TestUtils.createPlayer(getClass());
        chastityCageState = player.state(Toys.Chastity_Cage);
        pcm = player.state;
    }

    private void installMapping() {
        pcm.addScriptValueMapping(MappedScriptState.Global, new MappedScriptStateValue.Indefinitely(ChastityCageAction,
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
        assertThatUninitializedMultiMappedStateDefaultsToRemove();
        assertThatMultiMappedSetTimeSetsFlag();
        assertThatUnsetFlagResetsStateToRemoved();
        assertThatSetMultiMappedFlagSetsStateToAppliedTemporary();

        chastityCageState.remove();
        assertEquals(UNSET, pcm.get(ChastityCageAction));
    }

    @Test
    public void assertThatUninitializedMultiMappedStateDefaultsToRemove() {
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        assertFalse(chastityCageState.applied());
        assertTrue(chastityCageState.expired());
        assertTrue(((TeaseScriptPersistence.StateProxy) chastityCageState).peers().isEmpty());
        assertEquals(State.REMOVED, pcm.getTime(ChastityCageAction));
    }

    @Test
    public void assertThatMultiMappedSetTimeSetsFlag() {
        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        assertEquals(UNSET, pcm.get(ChastityCageAction));

        int expectedSeconds = 10;
        pcm.setTime(ChastityCageAction, player.duration(expectedSeconds, TimeUnit.SECONDS));

        assertEquals(expectedSeconds, chastityCageState.duration().remaining(TimeUnit.SECONDS));
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
    public void assertThatUnsetFlagResetsStateToRemoved() {
        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        pcm.unset(ChastityCageAction);
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        assertFalse(chastityCageState.applied());
        assertTrue(chastityCageState.expired());

        assertEquals(State.REMOVED, chastityCageState.duration().limit(TimeUnit.SECONDS));
        long chastityActionRemoveTime = pcm.getTime(ChastityCageAction);
        long time = player.teaseLib.getTime(TimeUnit.SECONDS);
        // assertEquals(State.REMOVED, chastityActionRemoveTime);

        assertEquals(State.REMOVED, chastityActionRemoveTime - time);
    }

    @Test
    public void assertThatSetMultiMappedFlagSetsStateToAppliedTemporary() {
        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

        pcm.set(ChastityCageAction);
        assertTrue(chastityCageState.applied());
        assertTrue(chastityCageState.expired());

        assertTrue(chastityCageState.is(Body.SomethingOnPenis));
        assertTrue(chastityCageState.is(Body.CannotJerkOff));

        long time = pcm.getTime(ChastityCageAction);
        assertEquals(player.teaseLib.getTime(TimeUnit.SECONDS), time);
    }

    @Test
    public void testThatScriptHandlesUnmappedTimeValuesCorrectly() throws AllActionsSetException,
            ScriptExecutionException, ScriptParsingException, ValidationIssue, IOException {
        Player testScript = TestUtils.createPlayer(getClass(), MAPPED_STATE_TEST_SCRIPT);

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
        assertEquals(State.TEMPORARY, chastityCageState.duration().limit(TimeUnit.SECONDS));
    }

    private void assertThatUninitializedStateHasCorrectDefaultValues() {
        assertEquals(UNSET, pcm.get(ChastityCageAction));
        assertFalse(chastityCageState.applied());
        assertTrue(chastityCageState.expired());
        assertTrue(((TeaseScriptPersistence.StateProxy) chastityCageState).peers().isEmpty());

        long seconds = pcm.getTime(ChastityCageAction);
        assertEquals(State.REMOVED, seconds);
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
        pcm.addScriptValueMapping(MappedScriptState.Global, new MappedScriptStateValue.Indefinitely(CondomsAction,
                condomState, Household.Condoms, Body.SomethingOnPenis));

        pcm.addStateTimeMapping(MappedScriptState.Global, CondomsAction, condomState, Household.Condoms,
                Body.SomethingOnPenis);

        pcm.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptStateValue.Indefinitely(SomethingOnPenisAction, condomState, Body.SomethingOnPenis));

        loadTestScript();

        TestUtils.play(player, 1050);
    }

    @Test
    public void testThatStatesWithMultiplePeersWorkAsExpectedWithSelfReferencingItemsInJava()
            throws ScriptParsingException, ValidationIssue, ScriptExecutionException, IOException {
        assertThatUninitializedStateHasCorrectDefaultValues();

        State condomState = player.state(Household.Condoms);
        pcm.addScriptValueMapping(MappedScriptState.Global, new MappedScriptStateValue.Indefinitely(CondomsAction,
                condomState, Household.Condoms, Body.SomethingOnPenis));

        pcm.addStateTimeMapping(MappedScriptState.Global, CondomsAction, condomState, Household.Condoms,
                Body.SomethingOnPenis);

        pcm.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptStateValue.Indefinitely(SomethingOnPenisAction, condomState, Body.SomethingOnPenis));

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
        Debugger debugger = new Debugger(player.teaseLib);
        debugger.freezeTime();

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
