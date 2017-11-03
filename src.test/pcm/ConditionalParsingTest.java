/**
 * 
 */
package pcm;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Locale;

import org.junit.Test;

import pcm.controller.Player;
import pcm.model.ActionRange;
import pcm.model.ScriptExecutionException;
import pcm.model.ScriptParsingException;
import pcm.model.ValidationIssue;
import pcm.state.persistence.ScriptState;
import pcm.util.TestUtils;
import teaselib.Actor;
import teaselib.Sexuality.Gender;
import teaselib.Sexuality.Sex;
import teaselib.core.TeaseLib;

/**
 * @author Citizen-Cane
 *
 */
public class ConditionalParsingTest {

    public Player createMistress(Sex sex, Gender gender) throws Exception {
        TeaseLib teaseLib = TestUtils.teaseLib();
        teaseLib.new PersistentEnum<>(TeaseLib.DefaultDomain, Sex.class).set(sex);
        teaseLib.new PersistentEnum<>(TeaseLib.DefaultDomain, Gender.class).set(gender);
        // TODO persistent enum needs class & default value,
        // otherwise The code looks strange
        // TODO set
        return createPlayer(teaseLib, teaseLib.getDominant(Gender.Feminine, Locale.US));
    }

    public Player createMaster(Sex sex, Gender gender) throws Exception {
        TeaseLib teaseLib = TestUtils.teaseLib();
        teaseLib.new PersistentEnum<>(TeaseLib.DefaultDomain, Sex.class).set(sex);
        teaseLib.new PersistentEnum<>(TeaseLib.DefaultDomain, Gender.class).set(gender);
        return createPlayer(teaseLib, teaseLib.getDominant(Gender.Masculine, Locale.US));
    }

    private static Player createPlayer(TeaseLib teaseLib, Actor dominant)
            throws ScriptParsingException, ValidationIssue, IOException, ScriptExecutionException {
        Player player = TestUtils.createPlayer(teaseLib, ConditionalParsingTest.class, dominant);
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
        TestUtils.play(player, r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if not parsed -", ScriptState.SET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testDominantFemalePath_Else_in_If_Else() throws Exception {
        Player player = createMistress(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(900);
        TestUtils.play(player, r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", ScriptState.SET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testDominantFemalePath_If_in_If_ElseIf_Else() throws Exception {
        Player player = createMistress(Sex.Female, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals("#if not parsed -", ScriptState.SET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testDominantFemalePath_ElseIf_in_If_ElseIf_Else() throws Exception {
        Player player = createMistress(Sex.Male, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif not parsed -", ScriptState.SET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testDominantFemalePath_Else_in_If_ElseIf_Else() throws Exception {
        Player player = createMistress(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", ScriptState.SET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testDominantMalePath_If_in_If_Else() throws Exception {
        Player player = createMaster(Sex.Female, Gender.Feminine);
        assertEquals(Sex.Female, player.persistentEnum(Sex.class).value());
        assertEquals(Gender.Feminine, player.persistentEnum(Gender.class).value());
        // todo player.get(Sexuality.Sex) to hide creation of persistent object
        ActionRange r = new ActionRange(900);
        TestUtils.play(player, r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if not parsed -", ScriptState.SET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testDominantMalePath_Else_in_If_Else() throws Exception {
        Player player = createMaster(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(900);
        TestUtils.play(player, r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", ScriptState.SET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testDominantMalePath_If_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sex.Female, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals("#if not parsed -", ScriptState.SET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));
        assertEquals(9999, player.range.start);

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testDominantMalePath_ElseIf_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sex.Male, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif not parsed -", ScriptState.SET, player.state.get(2));
        assertEquals("#else parsed -", ScriptState.UNSET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.range.start);
    }

    @Test
    public void testDominantMalePath_Else_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(901);
        TestUtils.play(player, r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.range.start);

        assertEquals("#if parsed -", ScriptState.UNSET, player.state.get(1));
        assertEquals("#elseif parsed -", ScriptState.UNSET, player.state.get(2));
        assertEquals("#else not parsed -", ScriptState.SET, player.state.get(3));

        assertEquals("after #else not parsed -", ScriptState.SET, player.state.get(4));
        assertEquals(9999, player.range.start);
    }
}
