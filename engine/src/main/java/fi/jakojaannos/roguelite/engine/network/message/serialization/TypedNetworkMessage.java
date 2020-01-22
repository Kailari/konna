package fi.jakojaannos.roguelite.engine.network.message.serialization;

import fi.jakojaannos.roguelite.engine.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessageType;
import lombok.Value;

/**
 * Wraps a generic network message of some {@link NetworkMessageType NetworkMessageType} to a
 * "wrapper" message, which can more easily be read/written from/to network channels.
 */
@Value
public class TypedNetworkMessage<TMessage extends NetworkMessage> {
    public static final int FIXED_HEADER_SIZE_IN_BYTES = 4 + 2;
    public static final int HEADER_MESSAGE_LENGTH_OFFSET = 4;

    NetworkMessageType<TMessage> messageType;
    TMessage message;
}
