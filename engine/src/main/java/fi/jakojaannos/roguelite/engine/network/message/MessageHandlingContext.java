package fi.jakojaannos.roguelite.engine.network.message;

import lombok.Value;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.network.NetworkConnection;

@Value
public class MessageHandlingContext {
    MainThread mainThread;

    NetworkConnection connection;
}
