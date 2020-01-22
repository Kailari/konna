package fi.jakojaannos.roguelite.engine.network.message;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.network.NetworkConnection;
import lombok.Value;

@Value
public class MessageHandlingContext {
    MainThread mainThread;

    NetworkConnection connection;
}
