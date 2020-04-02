package fi.jakojaannos.roguelite.engine.network.client;

import java.io.IOException;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.network.NetworkManager;
import fi.jakojaannos.roguelite.engine.network.client.internal.ClientCommandChannelRunnable;
import fi.jakojaannos.roguelite.engine.network.message.HelloMessage;
import fi.jakojaannos.roguelite.engine.network.message.MessageHandler;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageDecoder;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageEncoder;

public class ClientNetworkManager extends NetworkManager<ClientCommandChannelRunnable> {
    public ClientNetworkManager(
            final String host,
            final int port,
            final MainThread mainThread
    ) throws IOException {
        super(new ClientCommandChannelRunnable(host,
                                               port,
                                               MessageHandler.CLIENT_HANDLERS,
                                               new MessageEncoder(NetworkMessage.TYPES),
                                               new MessageDecoder(NetworkMessage.TYPES),
                                               mainThread));
        // FIXME: Remove this and implement proper tests
        send(new HelloMessage("Hello Netty!"));
    }

    public void send(final NetworkMessage message) {
        getCommandChannel().send(message);
    }
}
