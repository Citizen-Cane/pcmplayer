package pcm.state.visuals;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pcm.controller.Player;
import pcm.state.Visual;
import teaselib.core.devices.DeviceCache;
import teaselib.core.devices.release.Actuator;
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
    final long duration;

    public KeyReleaseHandler(String command) {
        this(command, 0);
    }

    public KeyReleaseHandler(String command, long seconds) {
        super();
        this.command = command;
        this.duration = seconds;
    }

    @Override
    public void render(Player player) {
        MediaRenderer keyReleaseHandler = new MediaRendererThread(player.teaseLib) {
            @Override
            protected void renderMedia() throws InterruptedException, IOException {
                logger.info("{} {}", command, (duration > 0 ? duration : ""));
                startCompleted();
                if (command.equalsIgnoreCase(Prepare)) {
                    prepare(player);
                } else {
                    handleKey(player);
                }
            }

            private void prepare(Player player) {
                player.script.clearKeyReleaseActuator();
                KeyRelease keyRelease = teaseLib.devices.get(KeyRelease.class).getDefaultDevice();
                mandatoryCompleted();

                if (DeviceCache.connect(keyRelease)) {
                    player.script.setKeyReleaseActuator(findActuatorForHoldingDuration(keyRelease));
                    Optional<Actuator> actuator = player.script.getKeyReleaseActuator();
                    if (actuator.isPresent() && !actuator.get().isRunning()) {
                        actuator.get().arm();
                    }
                }
            }

            private Actuator findActuatorForHoldingDuration(KeyRelease keyRelease) {
                return keyRelease.actuators().get(duration, TimeUnit.SECONDS);
            }

            private void handleKey(Player player) {
                Optional<Actuator> actuator = player.script.getKeyReleaseActuator();
                if (actuator.isPresent()) {
                    if (command.equalsIgnoreCase(Start)) {
                        actuator.get().start(duration, TimeUnit.SECONDS);
                    } else if (command.equalsIgnoreCase(Sleep)) {
                        actuator.get().sleep(duration, TimeUnit.SECONDS);
                    } else if (command.equalsIgnoreCase(Release)) {
                        actuator.get().release();
                    }
                }
                mandatoryCompleted();
            }
        };
        player.render(keyReleaseHandler);
    }
}
