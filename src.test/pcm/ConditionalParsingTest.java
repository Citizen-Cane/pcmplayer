package pcm;

import java.io.IOException;

import org.junit.jupiter.api.Test;

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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Citizen-Cane
 *
 */
public class ConditionalParsingTest {

    public TestPlayer createMistress(Sex sex, Gender gender) throws Exception {
        TeaseLib teaseLib = TestPlayer.teaseLib();
        teaseLib.getEnum(TeaseLib.DefaultDomain, Sex.class).set(sex);
        teaseLib.getEnum(TeaseLib.DefaultDomain, Gender.class).set(gender);
        // TODO persistent enum needs class & default value,
        // otherwise The code looks strange
        // TODO set
        return createPlayer(teaseLib, TestScript.newActor(Gender.Feminine));
    }

    public TestPlayer createMaster(Sex sex, Gender gender) throws Exception {
        TeaseLib teaseLib = TestPlayer.teaseLib();
        teaseLib.getEnum(TeaseLib.DefaultDomain, Sex.class).set(sex);
        teaseLib.getEnum(TeaseLib.DefaultDomain, Gender.class).set(gender);
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
        assertEquals(Sex.Female, player.persistence.newEnum(Sex.class).value());
        assertEquals(Gender.Feminine, player.persistence.newEnum(Gender.class).value());
        ActionRange r = new ActionRange(900);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals(ScriptState.SET, player.state.get(1), "#if not parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(2), "#elseif parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(3), "#else parsed -");

        assertEquals(ScriptState.SET, player.state.get(4), "after #else not parsed -");
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantFemalePath_Else_in_If_Else() throws Exception {
        TestPlayer player = createMistress(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(900);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals(ScriptState.UNSET, player.state.get(1), "#if parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(2), "#elseif parsed -");
        assertEquals(ScriptState.SET, player.state.get(3), "#else not parsed -");

        assertEquals(ScriptState.SET, player.state.get(4), "after #else not parsed -");
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantFemalePath_If_in_If_ElseIf_Else() throws Exception {
        Player player = createMistress(Sex.Female, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(ScriptState.SET, player.state.get(1), "#if not parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(2), "#elseif parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(3), "#else parsed -");

        assertEquals(ScriptState.SET, player.state.get(4), "after #else not parsed -");
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantFemalePath_ElseIf_in_If_ElseIf_Else() throws Exception {
        Player player = createMistress(Sex.Male, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals(ScriptState.UNSET, player.state.get(1), "#if parsed -");
        assertEquals(ScriptState.SET, player.state.get(2), "#elseif not parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(3), "#else parsed -");

        assertEquals(ScriptState.SET, player.state.get(4), "after #else not parsed -");
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantFemalePath_Else_in_If_ElseIf_Else() throws Exception {
        Player player = createMistress(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals(ScriptState.UNSET, player.state.get(1), "#if parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(2), "#elseif parsed -");
        assertEquals(ScriptState.SET, player.state.get(3), "#else not parsed -");

        assertEquals(ScriptState.SET, player.state.get(4), "after #else not parsed -");
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantMalePath_If_in_If_Else() throws Exception {
        Player player = createMaster(Sex.Female, Gender.Feminine);
        assertEquals(Sex.Female, player.persistence.newEnum(Sex.class).value());
        assertEquals(Gender.Feminine, player.persistence.newEnum(Gender.class).value());
        // todo player.get(Sexuality.Sex) to hide creation of persistent object
        ActionRange r = new ActionRange(900);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals(ScriptState.SET, player.state.get(1), "#if not parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(2), "#elseif parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(3), "#else parsed -");

        assertEquals(ScriptState.SET, player.state.get(4), "after #else not parsed -");
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantMalePath_Else_in_If_Else() throws Exception {
        Player player = createMaster(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(900);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals(ScriptState.UNSET, player.state.get(1), "#if parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(2), "#elseif parsed -");
        assertEquals(ScriptState.SET, player.state.get(3), "#else not parsed -");

        assertEquals(ScriptState.SET, player.state.get(4), "after #else not parsed -");
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantMalePath_If_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sex.Female, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(ScriptState.SET, player.state.get(1), "#if not parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(2), "#elseif parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(3), "#else parsed -");
        assertEquals(9999, player.action.number);

        assertEquals(ScriptState.SET, player.state.get(4), "after #else not parsed -");
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantMalePath_ElseIf_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sex.Male, Gender.Feminine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals(ScriptState.UNSET, player.state.get(1), "#if parsed -");
        assertEquals(ScriptState.SET, player.state.get(2), "#elseif not parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(3), "#else parsed -");

        assertEquals(ScriptState.SET, player.state.get(4), "after #else not parsed -");
        assertEquals(9999, player.action.number);
    }

    @Test
    public void testDominantMalePath_Else_in_If_ElseIf_Else() throws Exception {
        Player player = createMaster(Sex.Male, Gender.Masculine);
        ActionRange r = new ActionRange(901);
        player.play(r, r);

        assertEquals(ScriptState.SET, player.state.get(r.start));
        assertEquals(9999, player.action.number);

        assertEquals(ScriptState.UNSET, player.state.get(1), "#if parsed -");
        assertEquals(ScriptState.UNSET, player.state.get(2), "#elseif parsed -");
        assertEquals(ScriptState.SET, player.state.get(3), "#else not parsed -");

        assertEquals(ScriptState.SET, player.state.get(4), "after #else not parsed -");
        assertEquals(9999, player.action.number);
    }
}
