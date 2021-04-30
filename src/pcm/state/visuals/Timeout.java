/**
 * 
 */
package pcm.state.visuals;

import pcm.controller.Player;

/**
 * The timeout class is used to direct the delay to the interaction.
 * 
 * @author Citizen-Cane
 *
 */
public class Timeout extends Delay {

    public Timeout(int from, int to) {
        super(from, to);
    }

    public Timeout(int delay) {
        super(delay);
    }

    @Override
    public void render(Player player) {
        // The timeout class is used to direct the delay to the interaction
        duration = player.random.value(from, to);
    }

}
