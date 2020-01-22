package fi.jakojaannos.roguelite.engine.network.message.serialization;

import fi.jakojaannos.roguelite.engine.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessageType;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessageTypeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.ByteBuffer;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class MessageDecoder {
    private final NetworkMessageTypeMap typeMap;

    public Optional<TypedNetworkMessage<?>> decodeFromBuffer(final ByteBuffer bufferIn) {
        if (bufferIn.remaining() < TypedNetworkMessage.FIXED_HEADER_SIZE_IN_BYTES) {
            return Optional.empty();
        }

        bufferIn.mark();
        val typeId = bufferIn.getInt();
        val messageLength = bufferIn.getShort();
        if (bufferIn.remaining() < messageLength) {
            bufferIn.reset();
            return Optional.empty();
        }

        Optional<TypedNetworkMessage<?>> typedMessage = this.typeMap.getByMessageTypeId(typeId)
                                                                    .map(type -> deserializeToTypedWrapper(type, bufferIn));

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
