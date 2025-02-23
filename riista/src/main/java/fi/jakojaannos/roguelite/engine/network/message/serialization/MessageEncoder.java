package fi.jakojaannos.roguelite.engine.network.message.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import fi.jakojaannos.roguelite.engine.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessageType;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessageTypeMap;

public class MessageEncoder {
    private static final Logger LOG = LoggerFactory.getLogger(MessageEncoder.class);

    private final NetworkMessageTypeMap typeMap;

    public MessageEncoder(final NetworkMessageTypeMap typeMap) {
        this.typeMap = typeMap;
    }

    public void encodeToBuffer(
            final NetworkMessage message,
            final ByteBuffer bufferOut
    ) {
        this.typeMap.getByMessageClass(message.getClass())
                    .ifPresentOrElse(messageType -> serializeWithHeader(message, bufferOut, messageType),
                                     () -> LOG.error("Tried to write a message of an unregistered type: {}",
                                                     message.getClass().getSimpleName()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void serializeWithHeader(
            final NetworkMessage message,
            final ByteBuffer bufferOut,
            final NetworkMessageType<? extends NetworkMessage> messageType
    ) {
        // Write type ID
        bufferOut.putInt(messageType.getTypeId());

        // Write message
        bufferOut.position(TypedNetworkMessage.FIXED_HEADER_SIZE_IN_BYTES);
        ((NetworkMessageType) messageType).serialize(message, bufferOut);

        // Write the message length to header
        final var endIndex = bufferOut.position();
        final var messageLength = (short) (bufferOut.position() - TypedNetworkMessage.FIXED_HEADER_SIZE_IN_BYTES);
        bufferOut.position(TypedNetworkMessage.HEADER_MESSAGE_LENGTH_OFFSET);
        bufferOut.putShort(messageLength);

        // Reset the position to the end
        bufferOut.position(endIndex);
    }
}
