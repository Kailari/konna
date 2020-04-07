package fi.jakojaannos.roguelite.game.app;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.network.NetworkManager;
import fi.jakojaannos.roguelite.engine.network.ServerNetworkManager;

public class RogueliteServer {
    public static void run(
            final int port
    ) {
        try (final var runner = new ServerGameRunner(port)) {
            final Queue<InputEvent> dummyInputQueue = new ArrayDeque<>();
            runner.run(new ServerGameMode(), () -> dummyInputQueue);
        }
    }

    // FIXME: Get rid of this
    private static class ServerGameMode implements GameMode {
        @Override
        public GameState createState(final World world) {
            return new GameState(world);
        }

        @Override
        public void tick(final GameState state) {
        }

        @Override
        public void close() {
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
            state.world().registerResource(Network.class, new Network() {
                @Nullable private String error;

                @Override
                public Optional<NetworkManager<?>> getNetworkManager() {
                    return Optional.of(ServerGameRunner.this.networkManager);
                }

                @Override
                public Optional<String> getConnectionError() {
                    return Optional.ofNullable(this.error);
                }

                @Override
                public void setConnectionError(final String error) {
                    this.error = error;
                }
            });
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
