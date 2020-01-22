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
    static final Logger logger = LoggerFactory.getLogger(KeyReleaseHandler.class);

    public static final String Prepare = "prepare";
    public static final String Start = "start";
    public static final String Sleep = "sleep";
    public static final String Release = "release";

    public static final String[] Commands = { Prepare, Start, Sleep, Release };

    final String command;
    final long durationSeconds;

    public KeyReleaseHandler(String command) {
        this(command, 0);
    }

    public KeyReleaseHandler(String command, long durationSeconds) {
        super();
        this.command = command;
        this.durationSeconds = durationSeconds;
    }

    @Override
    public void render(Player player) {
        MediaRenderer keyReleaseHandler = new MediaRendererThread(player.teaseLib) {
            @Override
            protected void renderMedia() throws InterruptedException, IOException {
                logger.info("{} {}", command, (durationSeconds > 0 ? durationSeconds : ""));
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
                    Optional<Actuator> actuator = findActuatorForHoldingDuration(keyRelease);
                    if (actuator.isPresent()) {
                        player.script.setKeyReleaseActuator(actuator.get());
                        actuator.get().arm();
                    }
                }
            }

            private Optional<Actuator> findActuatorForHoldingDuration(KeyRelease keyRelease) {
                return keyRelease.actuators().available().get(durationSeconds, TimeUnit.SECONDS);
            }

            private void handleKey(Player player) {
                Optional<Actuator> actuator = player.script.getKeyReleaseActuator();
                if (actuator.isPresent()) {
                    if (command.equalsIgnoreCase(Start)) {
                        actuator.get().start(durationSeconds, TimeUnit.SECONDS);
                    } else if (command.equalsIgnoreCase(Sleep)) {
                        actuator.get().sleep(durationSeconds, TimeUnit.SECONDS);
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
