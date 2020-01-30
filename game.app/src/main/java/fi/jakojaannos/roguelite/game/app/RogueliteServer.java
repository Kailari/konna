package fi.jakojaannos.roguelite.game.app;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.network.ServerNetworkManager;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.RogueliteGame;

@Slf4j
public class RogueliteServer {
    public static void run(
            final int port
    ) throws Exception {
        try (final var game = new RogueliteGame()) {
            try (final var networkManager = new ServerNetworkManager(port, game);
                 final var runner = new ServerGameRunner(networkManager)
            ) {
                final Queue<InputEvent> dummyInputQueue = new ArrayDeque<>();
                final GameRunner.RendererFunction dummyRenderer = (state, partialTickAlpha, events) -> {};
                final Supplier<GameState> initialStateSupplier = RogueliteServer::createInitialGameState;

                runner.run(initialStateSupplier,
                           game,
                           () -> dummyInputQueue,
                           dummyRenderer);
            }
        }
    }

    private static ServerGameState createInitialGameState() {
        return new ServerGameState(World.createNew(EntityManager.createNew(256, 32)),
                                   new SimpleTimeManager(20L));
    }

    // FIXME: Get rid of this
    private static class ServerGameState extends GameState {
        ServerGameState(
                final World world,
                final TimeManager timeManager
        ) {
            super(world, timeManager);
        }

        @Override
        protected SystemDispatcher createDispatcher() {
            return SystemDispatcher.builder()
                                   .build();
        }
    }

    private static class ServerGameRunner extends GameRunner<RogueliteGame> {
        private final ServerNetworkManager networkManager;

        ServerGameRunner(final ServerNetworkManager networkManager) {
            this.networkManager = networkManager;
        }

        @Override
        protected boolean shouldContinueLoop(final RogueliteGame game) {
            return super.shouldContinueLoop(game) && this.networkManager.isConnected();
        }

        @Override
        public GameState simulateTick(
                final GameState state,
                final RogueliteGame game,
                final Events events
        ) {
            state.setNetworkManager(this.networkManager);
            return super.simulateTick(state, game, events);
        }

        @Override
        protected long getFramerateLimit() {
            return 1;
        }

        @Override
        protected long getMaxFrameTime() {
            return 2000L;
        }
    }
}
