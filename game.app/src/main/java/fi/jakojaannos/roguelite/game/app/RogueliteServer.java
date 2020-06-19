package fi.jakojaannos.roguelite.game.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayDeque;

import fi.jakojaannos.riista.GameRunnerTimeManager;
import fi.jakojaannos.riista.application.GameTicker;
import fi.jakojaannos.riista.data.resources.Network;
import fi.jakojaannos.riista.ecs.SystemDispatcher;
import fi.jakojaannos.riista.GameMode;
import fi.jakojaannos.riista.input.InputEvent;
import fi.jakojaannos.roguelite.engine.network.ServerNetworkManager;

public class RogueliteServer {
    private static final Logger LOG = LoggerFactory.getLogger(RogueliteServer.class);

    private static boolean killSwitch;
    private static ServerNetworkManager netman;

    public static void run(
            final int port
    ) {
        final var inputQueue = new ArrayDeque<InputEvent>();
        final var ticker = new GameTicker(new GameRunnerTimeManager(20L),
                                          () -> inputQueue,
                                          new GameMode(-1,
                                                       SystemDispatcher.builder().build(),
                                                       world -> {})) {
            @Override
            public void changeActiveGameMode(final GameMode gameMode) {
                super.changeActiveGameMode(gameMode);
                getState().world().fetchResource(Network.class).setNetworkManager(netman);
            }
        };

        try (final var networkManager = new ServerNetworkManager(port, ticker)) {
            // FIXME: Less hacky netman handling
            netman = networkManager;
            ticker.getState().world().fetchResource(Network.class).setNetworkManager(netman);
            while (!killSwitch && networkManager.isConnected()) {
                ticker.simulateTick(() -> killSwitch = true);
            }
        } catch (final IOException e) {
            LOG.error("Unexpected connection error occurred, server main thread crashed", e);
        }
    }
}
