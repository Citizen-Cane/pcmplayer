/**
 * 
 */
package pcm.state.visuals;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.state.Visual;
import teaselib.core.devices.DeviceCache;
import teaselib.core.devices.remote.KeyRelease;
import teaselib.core.media.MediaRenderer;
import teaselib.core.media.MediaRendererThread;

/**
 * @author Citizen-Cane
 *
 */
public class KeyReleaseHandler implements Visual {
    private static final Logger logger = LoggerFactory
            .getLogger(KeyReleaseHandler.class);
    public static final String Prepare = "prepare";
    public static final String Start = "start";
    public static final String Sleep = "sleep";
    public static final String Release = "release";

    public static final String[] Commands = { Prepare, Start, Sleep, Release };

    final String command;
    final int durationMinutes;

    public KeyReleaseHandler(String command) {
        this(command, 0);
    }

    public KeyReleaseHandler(String command, int durationSeconds) {
        super();
        this.command = command;
        this.durationMinutes = durationSeconds / 60;
    }

    @Override
    public void render(final Player player) {
        player.completeAll();
        logger.info(
                command + (durationMinutes > 0 ? " " + durationMinutes : ""));
        if (command.equalsIgnoreCase(Prepare)) {
            connectToDeviceAndArm(player);
        } else {
            if (player.keyRelease != null && player.keyReleaseActuator >= 0) {
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

    private void connectToDeviceAndArm(final Player player) {
        player.keyRelease = null;
        player.keyReleaseActuator = -1;
        MediaRenderer keyReleaseArm = new MediaRendererThread(player.teaseLib) {
            @Override
            protected void renderMedia()
                    throws InterruptedException, IOException {
                KeyRelease keyRelease = KeyRelease.Devices.getDefaultDevice();
                startCompleted();
                mandatoryCompleted();
                allCompleted();
                if (DeviceCache.connect(keyRelease)) {
                    player.keyRelease = keyRelease;
                    player.keyReleaseActuator = keyRelease
                            .getBestActuatorForTime(durationMinutes);
                    keyRelease.arm(player.keyReleaseActuator);
                }
            }

            @Override
            public void interrupt() {
                super.interrupt();
            }
        };
        player.render(keyReleaseArm);
    }
}
