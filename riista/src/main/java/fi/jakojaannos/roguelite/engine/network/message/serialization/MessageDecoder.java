package fi.jakojaannos.roguelite.engine.network.message.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Optional;

import fi.jakojaannos.roguelite.engine.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessageType;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessageTypeMap;

public class MessageDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(MessageDecoder.class);

    private final NetworkMessageTypeMap typeMap;

    public MessageDecoder(final NetworkMessageTypeMap typeMap) {
        this.typeMap = typeMap;
    }

    public Optional<TypedNetworkMessage<?>> decodeFromBuffer(final ByteBuffer bufferIn) {
        if (bufferIn.remaining() < TypedNetworkMessage.FIXED_HEADER_SIZE_IN_BYTES) {
            return Optional.empty();
        }

        bufferIn.mark();
        final var typeId = bufferIn.getInt();
        final var messageLength = bufferIn.getShort();
        if (bufferIn.remaining() < messageLength) {
            bufferIn.reset();
            return Optional.empty();
        }

        final Optional<TypedNetworkMessage<?>> typedMessage =
                this.typeMap.getByMessageTypeId(typeId)
                            .map(type -> deserializeToTypedWrapper(type,
                                                                   bufferIn));

        if (typedMessage.isEmpty()) {
            bufferIn.reset();
            LOG.error("Could not determine network message type of message with [typeID={}]", typeId);
        }

        return typedMessage;
    }

    protected <TMessage extends NetworkMessage> TypedNetworkMessage<TMessage> deserializeToTypedWrapper(
            final NetworkMessageType<TMessage> messageType,
            final ByteBuffer bufferIn
    ) {
        return new TypedNetworkMessage<>(messageType, messageType.deserialize(bufferIn));
    }
}
