package fi.jakojaannos.roguelite.engine.network.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.network.ServerNetworkManager;
import fi.jakojaannos.roguelite.engine.network.client.ClientNetworkManager;

public abstract class MessageHandler<TMessage extends NetworkMessage> {
    private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);
    public final static NetworkMessageHandlerMap SERVER_HANDLERS = new NetworkMessageHandlerMap(
            new MessageHandler<>(new HelloMessage.Type()) {
                @Override
                public void handle(
                        final HelloMessage message,
                        final MessageHandlingContext context
                ) {
                    LOG.info("Received HelloMessage: {}", message.hello());
                    context.mainThread().queueTask(state -> {
                        LOG.info("Handling on server!");
                        state.world()
                             .fetchResource(Network.class)
                             .getNetworkManager()
                             .map(ServerNetworkManager.class::cast)
                             .ifPresent(networkManager ->
                                                networkManager.send(context.connection(),
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
                    LOG.info("Received HelloMessage: {}", message.hello());
                    context.mainThread().queueTask(state -> {
                        LOG.info("On main thread?");
                        state.world()
                             .fetchResource(Network.class)
                             .getNetworkManager()
                             .map(ClientNetworkManager.class::cast)
                             .ifPresent(networkManager -> networkManager.send(
                                     new HelloMessage("Ping-Pong from client")));
                    });
                }
            }
    );
    private final NetworkMessageType<TMessage> messageType;

    public NetworkMessageType<TMessage> getMessageType() {
        return this.messageType;
    }

    protected MessageHandler(final NetworkMessageType<TMessage> messageType) {
        this.messageType = messageType;
    }

    public abstract void handle(TMessage message, MessageHandlingContext context);
}
