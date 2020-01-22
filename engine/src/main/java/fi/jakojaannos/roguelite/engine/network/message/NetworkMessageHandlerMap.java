package fi.jakojaannos.roguelite.engine.network.message;

import fi.jakojaannos.roguelite.engine.network.message.serialization.TypedNetworkMessage;
import lombok.val;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class NetworkMessageHandlerMap {
    private final Map<Integer, MessageHandler<?>> handlers;

    public NetworkMessageHandlerMap(final MessageHandler<?>... handlers) {
        this.handlers = Arrays.stream(handlers)
                              .collect(Collectors.toMap(handler -> handler.getMessageType().getTypeId(),
                                                        handler -> handler));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void tryHandle(
            final TypedNetworkMessage<?> message,
            final MessageHandlingContext context
    ) {
        val typeId = message.getMessageType().getTypeId();
        if (this.handlers.containsKey(typeId)) {
            MessageHandler handler = this.handlers.get(typeId);
            handler.handle(message.getMessage(), context);
        }
    }

}
