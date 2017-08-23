/**
 * 
 */
package pcm.state.visuals;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.state.Visual;
import teaselib.core.devices.DeviceCache;
import teaselib.core.devices.release.KeyRelease;
import teaselib.core.media.MediaRenderer;
import teaselib.core.media.MediaRendererThread;

/**
 * @author Citizen-Cane
 *
 */
public class KeyReleaseHandler implements Visual {
    private static final Logger logger = LoggerFactory.getLogger(KeyReleaseHandler.class);
    public static final String Prepare = "prepare";
    public static final String Start = "start";
    public static final String Sleep = "sleep";
    public static final String Release = "release";

    public static final String[] Commands = { Prepare, Start, Sleep, Release };

    final String command;
    final int duration;

    public KeyReleaseHandler(String command) {
        this(command, 0);
    }

    public KeyReleaseHandler(String command, int seconds) {
        super();
        this.command = command;
        this.duration = seconds;
    }

    @Override
    public void render(final Player player) {
        MediaRenderer keyReleaseArm = new MediaRendererThread(player.teaseLib) {
            @Override
            protected void renderMedia() throws InterruptedException, IOException {
                logger.info(command + (duration > 0 ? " " + duration : ""));
                startCompleted();
                if (command.equalsIgnoreCase(Prepare)) {
                    player.keyReleaseActuator = null;
                    KeyRelease keyRelease = teaseLib.devices.get(KeyRelease.class).getDefaultDevice();
                    mandatoryCompleted();
                    allCompleted();
                    if (DeviceCache.connect(keyRelease)) {
                        player.keyReleaseActuator = keyRelease.getActuator(duration, TimeUnit.SECONDS);
                        player.keyReleaseActuator.arm();
                    }
                } else {
                    if (player.keyReleaseActuator != null) {
                        if (command.equalsIgnoreCase(Start)) {
                            player.keyReleaseActuator.start(duration, TimeUnit.SECONDS);
                        } else if (command.equalsIgnoreCase(Sleep)) {
                            player.keyReleaseActuator.sleep(duration, TimeUnit.SECONDS);
                        } else if (command.equalsIgnoreCase(Release)) {
                            player.keyReleaseActuator.release();
                        }
                    }
                    mandatoryCompleted();
                    allCompleted();
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
