package fi.jakojaannos.roguelite.game.app;

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
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

@Slf4j
public class RogueliteServer {
    public static void run(
            final int port
    ) throws Exception {
        try (val game = new RogueliteGame()) {
            try (val networkManager = new ServerNetworkManager(port, game);
                 val runner = new ServerGameRunner(networkManager)
            ) {
                Queue<InputEvent> dummyInputQueue = new ArrayDeque<>();
                GameRunner.RendererFunction dummyRenderer = (state, partialTickAlpha, events) -> {};
                Supplier<GameState> initialStateSupplier = () -> new ServerGameState(World.createNew(EntityManager.createNew(256, 32)),
                                                                                     new SimpleTimeManager(20L));

                runner.run(initialStateSupplier,
                           game,
                           () -> dummyInputQueue,
                           dummyRenderer);
            }
        }
    }

    // FIXME: Get rid of this
    private static class ServerGameState extends GameState {
        public ServerGameState(
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

        public ServerGameRunner(final ServerNetworkManager networkManager) {
            this.networkManager = networkManager;
        }

        @Override
        protected boolean shouldContinueLoop(final RogueliteGame game) {
            return super.shouldContinueLoop(game) && networkManager.isConnected();
        }

        @Override
        public GameState simulateTick(
                final GameState state,
                final RogueliteGame game,
                final Events events
        ) {
            state.setNetworkManager(networkManager);
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
