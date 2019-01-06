package pcm;

import static org.junit.Assert.*;

import org.junit.Test;

import pcm.controller.AllActionsSetException;
import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.state.persistence.MappedScriptItemValue;
import pcm.state.persistence.MappedScriptState;
import pcm.state.persistence.MappedScriptStateValue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestUtils;
import teaselib.Toys;
import teaselib.core.TeaseLib;

public class PersistencyTest {

    enum Body {
        Chastified
    }

    @Test(expected = AllActionsSetException.class)
    public void testThatResetRangeUnsetsMappings() throws Exception {
        Player player = TestUtils.createPlayer(getClass());

        player.state.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptItemValue(365, player.items(Toys.Collar)));
        assertFalse(player.items(Toys.Collar).anyAvailable());

        player.item(Toys.Collar).setAvailable(true);
        assertTrue(player.items(Toys.Collar).anyAvailable());
        assertEquals(ScriptState.UNSET, player.state.get(365));

        player.validateScripts = false;
        player.loadScript("PersistencyTest_ResetRangeUnsetsMappings_MainScript");

        // Correct because the optimized .resetrange statement only clears previously restored items,
        // and thus won't clear the toy anymore
        assertTrue(player.items(Toys.Collar).anyAvailable());
        assertEquals(ScriptState.SET, player.state.get(365));

        player.state.unset(100);
        player.state.set(101);
        try {
            // This throws since when returning from the subscript,
            // action 365 has been set, and is thus cleared by .resetrange
            pcm.util.TestUtils.play(player, new ActionRange(1000), null);
        } catch (AllActionsSetException e) {
            assertEquals(1, e.actions.size());
            assertEquals(1001, e.actions.get(0).number);
            throw e;
        }
    }

    @Test
    public void testThatScriptRestoreWorks() throws Exception {
        Player player = TestUtils.createPlayer(getClass());

        player.state.addScriptValueMapping(MappedScriptState.Global,
                new MappedScriptItemValue(365, player.items(Toys.Collar)));
        assertFalse(player.items(Toys.Collar).anyAvailable());

        player.item(Toys.Collar).setAvailable(true);
        assertTrue(player.items(Toys.Collar).anyAvailable());
        assertEquals(ScriptState.UNSET, player.state.get(365));
        player.validateScripts = true;
        player.loadScript("PersistencyTest_ScriptRestoreWorks_MainScript");
        assertTrue(player.items(Toys.Collar).anyAvailable());
        assertEquals(ScriptState.SET, player.state.get(365));

        player.state.unset(100);
        player.state.set(101);
        pcm.util.TestUtils.play(player, new ActionRange(1000), null);
    }

    @Test
    public void testThatIndefiniteScriptValueMappingsArePersisted() throws Exception {
        Player player = TestUtils.createPlayer(getClass());
        String mainScript = "PersistencyTest_testThatMappedStateIsPersisted";
        MappedScriptState state = player.state;

        state.addScriptValueMapping(mainScript,
                new MappedScriptStateValue.ForSession(267, player.state(Body.Chastified), Toys.Chastity_Device));

        player.loadScript(mainScript);
        assertFalse(player.state(Body.Chastified).applied());
        assertFalse(player.state(Toys.Chastity_Device).applied());
        pcm.util.TestUtils.play(player, new ActionRange(1000), null);
        assertTrue(player.state(Body.Chastified).applied());
        assertTrue(player.state(Toys.Chastity_Device).applied());

        // Not persisted because not remembered
        TeaseLib.PersistentString chastifiedState = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                "pcm.PersistencyTest.Body", "Chastified.state.duration");
        assertFalse(chastifiedState.available());
    }

    @Test
    public void testThatSessionOnlyScriptValueMappingsAreLocal() throws Exception {
        Player player = TestUtils.createPlayer(getClass());
        String mainScript = "PersistencyTest_testThatMappedStateIsPersisted";
        MappedScriptState state = player.state;

        state.addScriptValueMapping(mainScript,
                new MappedScriptStateValue.ForSession(267, player.state(Body.Chastified), Toys.Chastity_Device));

        player.loadScript(mainScript);

        pcm.util.TestUtils.play(player, new ActionRange(1000), null);

        TeaseLib.PersistentString enemaState = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                "pcm.PersistencyTest.Body", "Chastified.state.duration");

        assertFalse(enemaState.available());
    }

    @Test
    public void testThatTimeMappingsPersistsPositiveValues() throws Exception {
        Player player = TestUtils.createPlayer(getClass());
        String mainScript = "PersistencyTest_testThatMappedStateIsPersisted";
        MappedScriptState state = player.state;

        state.addStateTimeMapping(MappedScriptState.Global, 45, player.state(Body.Chastified), Toys.Chastity_Device);

        player.loadScript(mainScript);

        pcm.util.TestUtils.play(player, new ActionRange(1000), null);

        // TODO namespace for variable is pcm.PersistencyTest.Body, but should be pcm.PersistencyTest
        TeaseLib.PersistentString chastifiedState = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                "pcm.PersistencyTest.Body", "Chastified.state.duration");
        assertTrue(chastifiedState.available());
    }

    @Test
    public void testThatTimeMappingDoesntPersist_SetTime_00_00_00() throws Exception {
        Player player = TestUtils.createPlayer(getClass());
        String mainScript = "PersistencyTest_testThatMappedStateIsPersisted";
        MappedScriptState state = player.state;

        state.addStateTimeMapping(MappedScriptState.Global, 45, player.state(Body.Chastified), Toys.Chastity_Device);

        player.loadScript(mainScript);

        pcm.util.TestUtils.play(player, new ActionRange(1010), null);

        TeaseLib.PersistentString chastifiedState = player.teaseLib.new PersistentString(TeaseLib.DefaultDomain,
                "pcm.PersistencyTest.Body", "Chastified.state.duration");
        assertFalse(chastifiedState.available());
    }
}
