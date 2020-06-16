package fi.jakojaannos.roguelite.game.app;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.riista.data.resources.Network;
import fi.jakojaannos.riista.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.network.ServerNetworkManager;

public class RogueliteServer {
    public static void run(
            final int port
    ) {
        try (final var runner = new ServerGameRunner(port)) {
            final Queue<InputEvent> dummyInputQueue = new ArrayDeque<>();
            runner.run(new GameMode(-1, SystemDispatcher.builder().build(), world -> {}),
                       () -> dummyInputQueue);
        }
    }

    private static class ServerGameRunner extends GameRunner implements AutoCloseable {
        private final int port;

        private ServerNetworkManager networkManager;

        @Override
        protected long getFramerateLimit() {
            return 1;
        }

        @Override
        protected long getMaxFrameTime() {
            return 2000L;
        }

        ServerGameRunner(final int port) {
            this.port = port;
        }

        @Override
        protected boolean shouldContinueLoop() {
            return this.networkManager.isConnected();
        }

        @Override
        public void run(final GameMode defaultGameMode, final InputProvider inputProvider) {
            try {
                this.networkManager = new ServerNetworkManager(this.port, this);
            } catch (final IOException e) {
                throw new IllegalStateException("Server network manager initialization crashed: " + e);
            }
            super.run(defaultGameMode, inputProvider);
        }

        @Override
        protected void onStateChange(final GameState state) {
            final var network = state.world().fetchResource(Network.class);
            if (network.getNetworkManager().isEmpty()) {
                network.setNetworkManager(this.networkManager);
            }
        }

        @Override
        protected void onModeChange(final GameMode gameMode) {
        }

        @Override
        public void close() {
            this.networkManager.close();
        }
    }
}
