package fi.jakojaannos.roguelite.engine.network;

import java.io.IOException;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.network.internal.ServerCommandChannelRunnable;
import fi.jakojaannos.roguelite.engine.network.message.MessageHandler;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageDecoder;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageEncoder;

public class ServerNetworkManager extends NetworkManager<ServerCommandChannelRunnable> {
    public ServerNetworkManager(final int port, final MainThread mainThread) throws IOException {
        super(new ServerCommandChannelRunnable(port,
                                               MessageHandler.SERVER_HANDLERS,
                                               new MessageEncoder(NetworkMessage.TYPES),
                                               new MessageDecoder(NetworkMessage.TYPES),
                                               mainThread));
    }

    public void send(final NetworkConnection target, final NetworkMessage message) {
        getCommandChannel().send(target, message);
    }
}
