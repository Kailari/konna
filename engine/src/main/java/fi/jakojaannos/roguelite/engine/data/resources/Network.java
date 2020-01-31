package fi.jakojaannos.roguelite.engine.data.resources;

import java.util.Optional;

import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;
import fi.jakojaannos.roguelite.engine.network.NetworkManager;

public interface Network extends ProvidedResource {
    Optional<NetworkManager<?>> getNetworkManager();

    Optional<String> getConnectionError();

    void setConnectionError(String error);
}
