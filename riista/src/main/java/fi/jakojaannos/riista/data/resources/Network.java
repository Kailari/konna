package fi.jakojaannos.riista.data.resources;

import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.network.NetworkManager;

public interface Network {
    Optional<NetworkManager<?>> getNetworkManager();

    void setNetworkManager(@Nullable NetworkManager<?> networkManager);

    Optional<String> getConnectionError();

    void setConnectionError(String error);
}
