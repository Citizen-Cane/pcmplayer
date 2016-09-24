/**
 * 
 */
package pcm.state.visuals;

import pcm.controller.Player;
import pcm.state.Visual;
import teaselib.core.devices.DeviceCache;

/**
 * @author Citizen-Cane
 *
 */
public class KeyRelease implements Visual {
    public static final String Prepare = "prepare";
    public static final String Start = "start";
    public static final String Sleep = "sleep";
    public static final String Release = "release";

    public static final String[] Commands = { Prepare, Start, Sleep, Release };

    final String command;
    final int durationMinutes;

    public KeyRelease(String command) {
        this(command, 0);
    }

    public KeyRelease(String command, int durationSeconds) {
        super();
        this.command = command;
        this.durationMinutes = durationSeconds / 60;
    }

    @Override
    public void render(Player player) {
        if (command.equalsIgnoreCase(Prepare)) {
            if (player.keyRelease == null) {
                player.keyRelease = teaselib.core.devices.remote.KeyRelease.Devices
                        .getDefaultDevice();
            }
            teaselib.core.devices.remote.KeyRelease keyRelease = player.keyRelease;
            if (DeviceCache.connect(keyRelease)) {
                player.keyReleaseActuator = keyRelease
                        .getBestActuatorForTime(durationMinutes);
                keyRelease.arm(player.keyReleaseActuator);
            }
        } else if (player.keyRelease != null
                && player.keyReleaseActuator >= 0) {
            if (command.equalsIgnoreCase(Start)) {
                player.keyRelease.start(player.keyReleaseActuator,
                        durationMinutes);
            } else if (command.equalsIgnoreCase(Sleep)) {
                player.keyRelease.sleep(durationMinutes);
            } else if (command.equalsIgnoreCase(Release)) {
                player.keyRelease.release(player.keyReleaseActuator);
            }
        }
    }
}
