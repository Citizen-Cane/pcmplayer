package pcm;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestPlayer;
import teaselib.Actor;
import teaselib.Sexuality.Gender;
import teaselib.Sexuality.Sex;
import teaselib.core.TeaseLib;
import teaselib.test.TestScript;

/**
 * @author Citizen-Cane
 *
 */
public class ConditionalParsingTest {

    public TestPlayer createMistress(Sex sex, Gender gender) throws Exception {
        TeaseLib teaseLib = TestPlayer.teaseLib();
        teaseLib.new PersistentEnum<>(TeaseLib.DefaultDomain, Sex.class).set(sex);
        teaseLib.new PersistentEnum<>(TeaseLib.DefaultDomain, Gender.class).set(gender);
        // TODO persistent enum needs class & default value,
        // otherwise The code looks strange
        // TODO set
        return createPlayer(teaseLib, TestScript.newActor(Gender.Feminine));
    }

    public TestPlayer createMaster(Sex sex, Gender gender) throws Exception {
        TeaseLib teaseLib = TestPlayer.teaseLib();
        teaseLib.new PersistentEnum<>(TeaseLib.DefaultDomain, Sex.class).set(sex);
        teaseLib.new PersistentEnum<>(TeaseLib.DefaultDomain, Gender.class).set(gender);
        return createPlayer(teaseLib, TestScript.newActor(Gender.Masculine));
    }

    private static TestPlayer createPlayer(TeaseLib teaseLib, Actor dominant)
            throws ScriptParsingException, ValidationIssue, IOException, ScriptExecutionException {
        TestPlayer player = new TestPlayer(teaseLib, ConditionalParsingTest.class, dominant);
        player.loadScript("ConditionalParsingTest");
        assertEquals(3, player.script.actions.size());
        return player;
    }

    @Test
    public void testDominantFemalePath_If_in_If_Else() throws Exception {
        Player player = createMistress(Sex.Female, Gender.Feminine);
        assertEquals(Sex.Female, player.persistentEnum(Sex.class).value());
        assertEquals(Gender.Feminine, player.persistentEnum(Gender.class).value());
        ActionRange r = new ActionRange(900);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals("#if not parsed -", ScriptState.SET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantFemalePath_Else_in_If_Else() throws Exception {
        TestPlayer player = createMistress(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(900);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", ScriptState.SET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantFemalePath_If_in_If_ElseIf_Else() throws Exception {
        Player player = createMistress(Sex.Female, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals("#if not parsed -", ScriptState.SET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantFemalePath_ElseIf_in_If_ElseIf_Else() throws Exception {
        Player player = createMistress(Sex.Male, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif not parsed -", ScriptState.SET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantFemalePath_Else_in_If_ElseIf_Else() throws Exception {
        Player player = createMistress(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", ScriptState.SET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantMalePath_If_in_If_Else() throws Exception {
        Player player = createMaster(Sex.Female, Gender.Feminine);
        assertEquals(Sex.Female, player.persistentEnum(Sex.class).value());
        assertEquals(Gender.Feminine, player.persistentEnum(Gender.class).value());
        // todo player.get(Sexuality.Sex) to hide creation of persistent object
        ActionRange r = new ActionRange(900);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals("#if not parsed -", ScriptState.SET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantMalePath_Else_in_If_Else() throws Exception {
        Player player = createMaster(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(900);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", ScriptState.SET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantMalePath_If_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sex.Female, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals("#if not parsed -", ScriptState.SET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));
        assertEquals(9999, player.action.number);

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantMalePath_ElseIf_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sex.Male, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif not parsed -", ScriptState.SET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantMalePath_Else_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", ScriptState.SET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.action.number);
    }
}
