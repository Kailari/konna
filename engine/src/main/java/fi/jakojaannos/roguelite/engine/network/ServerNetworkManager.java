package fi.jakojaannos.roguelite.engine.network;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.network.internal.ServerCommandChannelRunnable;
import fi.jakojaannos.roguelite.engine.network.message.MessageHandler;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageDecoder;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
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
