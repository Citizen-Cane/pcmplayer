package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;
import teaselib.Actor;
import teaselib.text.Message;

/**
 * @author someone
 *
 *         Instructions are used in several ways: - As a longer formated text,
 *         without an image example: self bondage warning, action 1038++
 *         example: corner time instructions, action mine-paddle, 2020++ - A a
 *         short note together with an image, for additional immediate
 *         instructions that can't be given via message or choice buttons
 *         Example: cum quick - the .txt is used to announce that the stop
 *         button has to be pressed immediately Example: dildo training - the
 *         .txt is press the stop button once the dildo is inserted all the way
 *         in Example: tease- and denial the .txt is press the stop button once
 *         the dildo is inserted all the way in
 * 
 *         The self bondage text can be transformed into a lecture and be spoken
 *         The corner time text is intended to not be spoken Single line .txt
 *         statements can be transformed into choice texts (that wasn't possible
 *         with good old PCM. So an instructional text is one that isn't spoken,
 *         and only to be used in mine-paddle to restate the corner time rules
 */
public class Txt implements Visual {
    public final Message txt;

    public Txt(Actor actor) {
        txt = new Message(actor);
    }

    public void add(String line) {
        txt.add(line);
    }

    @Override
    public void render(Player player) {
        player.show(txt.toString());
    }
}
