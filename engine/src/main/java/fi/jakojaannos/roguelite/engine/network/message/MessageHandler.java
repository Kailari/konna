package fi.jakojaannos.roguelite.engine.network.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import fi.jakojaannos.roguelite.engine.network.ServerNetworkManager;
import fi.jakojaannos.roguelite.engine.network.client.ClientNetworkManager;

@Slf4j
@RequiredArgsConstructor
public abstract class MessageHandler<TMessage extends NetworkMessage> {
    public final static NetworkMessageHandlerMap SERVER_HANDLERS = new NetworkMessageHandlerMap(
            new MessageHandler<>(new HelloMessage.Type()) {
                @Override
                public void handle(
                        final HelloMessage message,
                        final MessageHandlingContext context
                ) {
                    LOG.info("Received HelloMessage: {}", message.hello);
                    context.getMainThread().queueTask(state -> {
                        LOG.info("Handling on server!");
                        state.getNetworkManager()
                             .map(ServerNetworkManager.class::cast)
                             .ifPresent(networkManager ->
                                                networkManager.send(context.getConnection(),
                                                                    new HelloMessage("Ping-Pong from server")));
                    });
                }
            }
    );

    public static final NetworkMessageHandlerMap CLIENT_HANDLERS = new NetworkMessageHandlerMap(
            new MessageHandler<>(new HelloMessage.Type()) {
                @Override
                public void handle(
                        final HelloMessage message,
                        final MessageHandlingContext context
                ) {
                    LOG.info("Received HelloMessage: {}", message.hello);
                    context.getMainThread().queueTask(state -> {
                        LOG.info("On main thread?");
                        state.getNetworkManager()
                             .map(ClientNetworkManager.class::cast)
                             .ifPresent(networkManager -> networkManager.send(
                                     new HelloMessage("Ping-Pong from client")));
                    });
                }
            }
    );

    @Getter private final NetworkMessageType<TMessage> messageType;

    public abstract void handle(TMessage message, MessageHandlingContext context);
}
