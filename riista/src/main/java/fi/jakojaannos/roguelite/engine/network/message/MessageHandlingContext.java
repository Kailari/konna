package fi.jakojaannos.roguelite.engine.network.message;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.network.NetworkConnection;

public record MessageHandlingContext(MainThread mainThread, NetworkConnection connection) {
}
