package fi.jakojaannos.roguelite.game.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import fi.jakojaannos.konna.view.KonnaGameModeRenderers;
import fi.jakojaannos.riista.vulkan.application.ApplicationRunner;
import fi.jakojaannos.riista.vulkan.application.VulkanApplication;
import fi.jakojaannos.riista.vulkan.assets.storage.VulkanAssetManager;
import fi.jakojaannos.riista.vulkan.input.GLFWInputProvider;
import fi.jakojaannos.riista.vulkan.renderer.VulkanGameRenderAdapter;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;

public class RogueliteClient {
    private static final Logger LOG = LoggerFactory.getLogger(RogueliteClient.class);

    public static void run(
            final Path assetRoot,
            final String host,
            final int port,
            final int windowWidth,
            final int windowHeight
    ) {
        LOG.trace("Running application");
        LOG.debug("asset root: {}", assetRoot);

        try (final var app = VulkanApplication.initialize(windowWidth <= 0 ? 800 : windowWidth,
                                                          windowHeight <= 0 ? 600 : windowHeight);
             final var assetManager = new VulkanAssetManager(app.backend(),
                                                             app.window(),
                                                             assetRoot);
             final var runner = new ApplicationRunner(app, assetManager);
             final var renderer = new VulkanGameRenderAdapter(KonnaGameModeRenderers.create(assetManager,
                                                                                            runner.getTimeManager(),
                                                                                            runner.getAudioContext()),
                                                              app,
                                                              runner::updateCameraProperties)
        ) {
            runner.run(new GLFWInputProvider(app.window()),
                       MainMenuGameMode.create(host, port),
                       renderer);
        }
    }
}
