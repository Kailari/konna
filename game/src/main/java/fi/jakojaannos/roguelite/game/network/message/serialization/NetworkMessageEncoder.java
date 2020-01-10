package fi.jakojaannos.roguelite.game.network.message.serialization;

import fi.jakojaannos.roguelite.game.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.game.network.message.NetworkMessageType;
import fi.jakojaannos.roguelite.game.network.message.NetworkMessageTypeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
@RequiredArgsConstructor
public class NetworkMessageEncoder {
    private final NetworkMessageTypeMap typeMap;

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void encode(
            final NetworkMessage message,
            final ByteBuffer bufferOut
    ) {
        this.typeMap.getByMessageClass(message.getClass())
                    .map(networkMessageType -> TypedNetworkMessage.forWriting((NetworkMessageType) networkMessageType, message))
                    .ifPresentOrElse(typedNetworkMessage -> typedNetworkMessage.write(bufferOut),
                                     () -> LOG.error("Tried to write a message of an unregistered type: {}",
                                                     message.getClass().getSimpleName()));
    }
}
