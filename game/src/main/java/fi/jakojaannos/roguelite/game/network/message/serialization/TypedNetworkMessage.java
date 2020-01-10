package fi.jakojaannos.roguelite.game.network.message.serialization;

import fi.jakojaannos.roguelite.game.network.message.MessageHandlingContext;
import fi.jakojaannos.roguelite.game.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.game.network.message.NetworkMessageType;
import fi.jakojaannos.roguelite.game.network.message.NetworkMessageTypeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Wraps a generic network message of some {@link NetworkMessageType NetworkMessageType} to a
 * "wrapper" message, which can more easily be read/written from/to network channels.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TypedNetworkMessage<TMessage extends NetworkMessage> {
    public static final int HEADER_SIZE_IN_BYTES = 4;

    @Getter private final NetworkMessageType<TMessage> messageType;
    @Getter private final TMessage message;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Optional<TypedNetworkMessage<?>> forReading(
            final NetworkMessageTypeMap typeMap,
            final ByteBuffer bufferIn
    ) {
        val typeId = bufferIn.getInt();
        return typeMap.getByMessageTypeId(typeId)
                      // FIXME: Allow variable length messages by passing in bufferIn to
                      //  the getSizeInBytes() and providing a separate getHeaderSizeInBytes()
                      .filter(type -> bufferIn.limit() >= type.getSizeInBytes())
                      .map(type -> new TypedNetworkMessage(type, type.deserialize(bufferIn)));
    }

    public static <TMessage extends NetworkMessage> TypedNetworkMessage<TMessage> forWriting(
            final NetworkMessageType<TMessage> messageType,
            final TMessage message
    ) {
        return new TypedNetworkMessage<>(messageType, message);
    }

    public void write(final ByteBuffer bufferOut) {
        bufferOut.putInt(this.messageType.getTypeId());
        this.messageType.serialize(this.message, bufferOut);
    }

    public void handle(final MessageHandlingContext context) {
        this.messageType.handle(this.message, context);
    }
}
