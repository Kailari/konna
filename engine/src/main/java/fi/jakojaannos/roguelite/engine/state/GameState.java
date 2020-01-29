package fi.jakojaannos.roguelite.engine.state;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.network.NetworkManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class GameState implements WorldProvider, AutoCloseable {
    @Getter private final World world;

    @Nullable private NetworkManager<?> networkManager;
    private final SystemDispatcher dispatcher;

    public Optional<NetworkManager<?>> getNetworkManager() {
        return Optional.ofNullable(this.networkManager);
    }

    public void setNetworkManager(@Nullable final NetworkManager<?> networkManager) {
        this.networkManager = networkManager;
        // TODO: null the netman on disconnect
        // this.networkManager.onDisconnect(this::handleNetworkManagerDisconnect);
    }

    public GameState(
            final World world,
            final TimeManager timeManager
    ) {
        this.world = world;
        this.world.createOrReplaceResource(Time.class, new Time(timeManager));

        this.dispatcher = createDispatcher();
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
