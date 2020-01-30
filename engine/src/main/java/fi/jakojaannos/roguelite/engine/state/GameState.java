package fi.jakojaannos.roguelite.engine.state;

import lombok.Getter;

import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.network.NetworkManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public abstract class GameState implements WorldProvider, AutoCloseable {
    @Getter private final World world;
    private final SystemDispatcher dispatcher;
    @Nullable private NetworkManager<?> networkManager;

    public GameState(
            final World world,
            final TimeManager timeManager
    ) {
        this.world = world;
        this.world.createOrReplaceResource(Time.class, new Time(timeManager));

        this.dispatcher = createDispatcher();
    }

    public Optional<NetworkManager<?>> getNetworkManager() {
        return Optional.ofNullable(this.networkManager);
    }

    public void setNetworkManager(@Nullable final NetworkManager<?> networkManager) {
        this.networkManager = networkManager;
        // TODO: null the netman on disconnect
        // this.networkManager.onDisconnect(this::handleNetworkManagerDisconnect);
    }

    protected abstract SystemDispatcher createDispatcher();

    public void tick() {
        this.dispatcher.dispatch(this.world);
        this.world.getEntityManager().applyModifications();
    }

    @Override
    public void close() throws Exception {
        getNetworkManager().ifPresent(NetworkManager::close);
        this.dispatcher.close();
    }
}
