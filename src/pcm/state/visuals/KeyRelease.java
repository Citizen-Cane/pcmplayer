/**
 * 
 */
package pcm.state.visuals;

import java.io.IOException;

import pcm.controller.Player;
import pcm.state.Visual;
import teaselib.core.devices.DeviceCache;
import teaselib.core.media.MediaRenderer;
import teaselib.core.media.MediaRendererThread;

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

    // TODO blocks script until key release is connected -> own thread
    @Override
    public void render(final Player player) {
        if (command.equalsIgnoreCase(Prepare)) {
            player.keyRelease = null;
            player.keyReleaseActuator = -1;
            MediaRenderer keyReleaseArm = new MediaRendererThread(
                    player.teaseLib) {
                @Override
                protected void renderMedia()
                        throws InterruptedException, IOException {
                    teaselib.core.devices.remote.KeyRelease keyRelease = teaselib.core.devices.remote.KeyRelease.Devices
                            .getDefaultDevice();
                    startCompleted();
                    mandatoryCompleted();
                    if (DeviceCache.connect(keyRelease)) {
                        player.keyRelease = keyRelease;
                        player.keyReleaseActuator = keyRelease
                                .getBestActuatorForTime(durationMinutes);
                        keyRelease.arm(player.keyReleaseActuator);
                    }
                    allCompleted();
                }

                @Override
                public void interrupt() {
                    super.interrupt();
                }
            };
            player.render(keyReleaseArm);
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
