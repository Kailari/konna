package fi.jakojaannos.konna;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;

import fi.jakojaannos.konna.engine.application.Application;
import fi.jakojaannos.konna.engine.application.ApplicationRunner;
import fi.jakojaannos.konna.engine.assets.storage.AssetManagerImpl;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;

public class Main {
    public static void main(final String[] args) {
        try (final var app = Application.initialize(800,
                                                    600);
             final var assetManager = new AssetManagerImpl(app.backend(),
                                                           Path.of("../assets"));
             final var runner = new ApplicationRunner(app, assetManager)
        ) {
            /*
            final var inputProvider = new LWJGLInputProvider(app.window().getHandle(),
                                                              800,
                                                              600,
                                                              legacyCallback -> app.window().onResize(legacyCallback::call));
             */
            final var inputProvider = new InputProvider() {
                @Override
                public Object getLock() {
                    return this;
                }

                @Override
                public Queue<InputEvent> pollEvents() {
                    // TODO: Adapt/re-write the LWJGL input provider
                    return new ArrayDeque<>();
                }
            };

            final var timeManager = runner.getTimeManager();
            runner.run(inputProvider,
                       GameplayGameMode.create(420, timeManager));
        }
    }
}
