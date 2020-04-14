package fi.jakojaannos.roguelite.engine.network;

import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.data.resources.Network;

public class NetworkImpl implements Network {
    @Nullable
    private NetworkManager<?> networkManager;

    @Nullable
    private String connectionError;

    @Override
    public Optional<NetworkManager<?>> getNetworkManager() {
        return Optional.ofNullable(this.networkManager);
    }

    @Override
    public void setNetworkManager(@Nullable final NetworkManager<?> networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public Optional<String> getConnectionError() {
        return Optional.ofNullable(this.connectionError);
    }

    @Override
    public void setConnectionError(@Nullable final String error) {
        this.connectionError = error;
    }
}
