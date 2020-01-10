package fi.jakojaannos.roguelite.game.network.message.serialization;

import fi.jakojaannos.roguelite.game.network.message.NetworkMessageTypeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class NetworkMessageDecoder {
    private final NetworkMessageTypeMap typeMap;

    public boolean decode(
            final ByteBuffer bufferIn,
            final Consumer<TypedNetworkMessage<?>> messageConsumer
    ) {

        if (bufferIn.remaining() < TypedNetworkMessage.HEADER_SIZE_IN_BYTES) {
            return false;
        }

        bufferIn.mark();
        TypedNetworkMessage.forReading(this.typeMap, bufferIn)
                           .ifPresentOrElse(messageConsumer,
                                            () -> {
                                                bufferIn.reset();
                                                val typeId = bufferIn.getInt();
                                                bufferIn.reset();
                                                LOG.error("Could not determine network message type of message with [typeID={}]",
                                                          typeId);
                                            });

        return true;
    }
}
