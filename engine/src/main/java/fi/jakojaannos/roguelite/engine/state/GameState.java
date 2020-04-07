package fi.jakojaannos.roguelite.engine.state;

import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.network.NetworkManager;

public abstract class GameState implements WorldProvider, AutoCloseable {
    private final World world;
    private final SystemDispatcher dispatcher;
    @Nullable private NetworkManager<?> networkManager;

    @Override
    public World getWorld() {
        return this.world;
    }

    public Optional<NetworkManager<?>> getNetworkManager() {
        return Optional.ofNullable(this.networkManager);
    }

    public void setNetworkManager(@Nullable final NetworkManager<?> networkManager) {
        this.networkManager = networkManager;
        // TODO: null the netman on disconnect
        // this.networkManager.onDisconnect(this::handleNetworkManagerDisconnect);
    }

    public GameState(
            final World world
    ) {
        this.world = world;
        this.world.registerResource(Network.class, new Network() {
            @Nullable private String error;

            @Override
            public Optional<NetworkManager<?>> getNetworkManager() {
                return GameState.this.getNetworkManager();
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

        this.dispatcher = createDispatcher();
    }

    protected abstract SystemDispatcher createDispatcher();

    public void tick() {
        this.dispatcher.tick(this.world);
        this.world.getEntityManager().applyModifications();
    }

    @Override
    public void close() throws Exception {
        getNetworkManager().ifPresent(NetworkManager::close);
        this.dispatcher.close();
    }
}
