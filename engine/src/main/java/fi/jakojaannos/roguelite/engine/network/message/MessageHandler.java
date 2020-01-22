package fi.jakojaannos.roguelite.engine.network.message;

import fi.jakojaannos.roguelite.engine.network.ServerNetworkManager;
import fi.jakojaannos.roguelite.engine.network.client.ClientNetworkManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class MessageHandler<TMessage extends NetworkMessage> {
    public final static NetworkMessageHandlerMap SERVER_HANDLERS = new NetworkMessageHandlerMap(
            new MessageHandler<>(new NetworkMessage.HelloMessage.Type()) {
                @Override
                public void handle(
                        final NetworkMessage.HelloMessage message,
                        final MessageHandlingContext context
                ) {
                    LOG.info("Received HelloMessage: {}", message.hello);
                    context.getMainThread().queueTask(state -> {
                        LOG.info("Handling on server!");
                        state.getNetworkManager()
                             .map(ServerNetworkManager.class::cast)
                             .ifPresent(networkManager -> networkManager.send(context.getConnection(), new NetworkMessage.HelloMessage("Ping-Pong from server")));
                    });
                }
            }
    );

    public static final NetworkMessageHandlerMap CLIENT_HANDLERS = new NetworkMessageHandlerMap(
            new MessageHandler<>(new NetworkMessage.HelloMessage.Type()) {
                @Override
                public void handle(
                        final NetworkMessage.HelloMessage message,
                        final MessageHandlingContext context
                ) {
                    LOG.info("Received HelloMessage: {}", message.hello);
                    context.getMainThread().queueTask(state -> {
                        LOG.info("On main thread?");
                        state.getNetworkManager()
                             .map(ClientNetworkManager.class::cast)
                             .ifPresent(networkManager -> networkManager.send(new NetworkMessage.HelloMessage("Ping-Pong from client")));
                    });
                }
            }
    );


    @Getter private final NetworkMessageType<TMessage> messageType;

    public abstract void handle(TMessage message, MessageHandlingContext context);
}
