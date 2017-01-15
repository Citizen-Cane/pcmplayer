package pcm;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import pcm.controller.Player;
import pcm.controller.ProbabilityModel;
import pcm.controller.ProbabilityModelBasedOnPossBucketSum;
import pcm.model.Action;
import pcm.model.ActionRange;
import pcm.util.TestUtils;

public class PossTest {
    private final AtomicInteger randomResult = new AtomicInteger(0);
    private final ProbabilityModel probabilityModel = new ProbabilityModelBasedOnPossBucketSum() {
        @Override
        protected double random(double from, double to) {
            return randomResult.get();
        }
    };

    @Test
    public void testPoss() throws Exception {
        Player player = TestUtils.createPlayer(getClass(), "PossTest");

        ActionRange r = new ActionRange(1000, 1003);
        List<Action> actions = player.range(r);
        assertEquals(4, actions.size());
        assertEquals(10, actions.get(0).poss.intValue());
        assertEquals(20, actions.get(1).poss.intValue());
        assertEquals(30, actions.get(2).poss.intValue());
        assertEquals(null, actions.get(3).poss);

        assertDecision(actions.get(0), actions, 0);
        assertDecision(actions.get(0), actions, 9);
        assertDecision(actions.get(0), actions, 10);

        assertDecision(actions.get(1), actions, 11);
        assertDecision(actions.get(1), actions, 30);

        assertDecision(actions.get(2), actions, 31);
        assertDecision(actions.get(2), actions, 60);

        assertDecision(actions.get(3), actions, 61);
        assertDecision(actions.get(3), actions, 99);
        assertDecision(actions.get(3), actions, 100);
    }

    @Test
    public void testPoss100() throws Exception {
        Player player = TestUtils.createPlayer(getClass(), "PossTest");

        ActionRange r = new ActionRange(1010, 1013);
        List<Action> actions = player.range(r);

        assertEquals(1, actions.size());
        assertEquals(100, actions.get(0).poss.intValue());

        assertDecision(actions.get(0), actions, 0);
        assertDecision(actions.get(0), actions, 50);
        assertDecision(actions.get(0), actions, 100);
    }

    @Test
    public void testPossElse() throws Exception {
        Player player = TestUtils.createPlayer(getClass(), "PossTest");

        player.state.unset(0);
        player.state.unset(1);
        player.state.unset(2);
        player.state.unset(3);
        player.state.set(4);
        player.state.set(5);
        ActionRange r = new ActionRange(1020, 1025);
        List<Action> actions = player.range(r);

        assertEquals(2, actions.size());
        assertEquals(0, actions.get(0).poss.intValue());
        assertEquals(0, actions.get(1).poss.intValue());

        assertDecision(actions.get(0), actions, 0);
        assertDecision(actions.get(0), actions, 50);

        assertDecision(actions.get(1), actions, 51);
        assertDecision(actions.get(1), actions, 100);
    }

    @Test
    public void testPoss100WeightedElse() throws Exception {
        Player player = TestUtils.createPlayer(getClass(), "PossTest");

        player.state.set(0);
        player.state.set(1);
        player.state.set(2);
        player.state.set(3);
        player.state.set(4);
        player.state.set(5);
        ActionRange r = new ActionRange(1020, 1025);
        List<Action> actions = player.range(r);

        assertEquals(1, actions.size());
        assertEquals(100, actions.get(0).poss.intValue());

        assertDecision(actions.get(0), actions, 0);
        assertDecision(actions.get(0), actions, 100);
    }

    @Test
    public void testWeightedElse() throws Exception {
        Player player = TestUtils.createPlayer(getClass(), "PossTest");

        player.state.unset(0);
        player.state.set(1);
        player.state.set(2);
        player.state.set(3);
        player.state.set(4);
        player.state.set(5);
        ActionRange r = new ActionRange(1020, 1025);
        List<Action> actions = player.range(r);

        assertEquals(3, actions.size());
        assertEquals(10, actions.get(0).poss.intValue());
        assertEquals(15, actions.get(1).poss.intValue());
        assertEquals(25, actions.get(2).poss.intValue());

        assertDecision(actions.get(0), actions, 0);
        assertDecision(actions.get(0), actions, 20);

        assertDecision(actions.get(1), actions, 21);
        assertDecision(actions.get(1), actions, 50);

        assertDecision(actions.get(2), actions, 51);
        assertDecision(actions.get(2), actions, 100);
    }

    @Test
    public void testElse2() throws Exception {
        Player player = TestUtils.createPlayer(getClass(), "PossTest");

        player.state.unset(0);
        player.state.unset(1);
        player.state.unset(2);
        player.state.unset(3);
        player.state.set(4);
        player.state.set(5);
        ActionRange r = new ActionRange(1020, 1025);
        List<Action> actions = player.range(r);

        assertEquals(2, actions.size());
        assertEquals(0, actions.get(0).poss.intValue());
        assertEquals(0, actions.get(1).poss.intValue());

        assertDecision(actions.get(0), actions, 0);
        assertDecision(actions.get(0), actions, 50);

        assertDecision(actions.get(1), actions, 51);
        assertDecision(actions.get(1), actions, 100);
    }

    private void assertDecision(Action expectedAction, List<Action> actions,
            int randomValue) {
        synchronized (randomResult) {
            randomResult.set(randomValue);
            Action actionResult = probabilityModel
                    .chooseActionBasedOnPossValue(actions);
            assertEquals(expectedAction, actionResult);
        }
    }

}
