package fi.jakojaannos.roguelite.game.network.client;

import fi.jakojaannos.roguelite.game.LogCategories;
import fi.jakojaannos.roguelite.game.network.CommandChannel;
import fi.jakojaannos.roguelite.game.network.message.MessageHandlingContext;
import fi.jakojaannos.roguelite.game.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.game.network.message.NetworkMessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ClientNetworkManager implements NetworkManager {
    @Nullable private CommandChannel commandChannel;

    public void connect(final String host, final int port) {
        LOG.debug("Connecting to {}:{}", host, port);
        if (this.commandChannel != null) {
            LOG.error(LogCategories.NET_CONNECTION, "Could not connect: Already connected to some host!");
            return;
        }

        this.commandChannel = new CommandChannel(host, port);
        this.commandChannel.send(new HelloMessage("Hello Netty!"));
    }

    @Override
    public boolean isConnected() {
        return this.commandChannel != null && this.commandChannel.isConnected();
    }

    @Override
    public void handleMessageTasksAndFlush() {
        if (this.commandChannel == null) {
            return;
        }

        val tasks = this.commandChannel.pollMessageTasks();
        for (val task : tasks) {
            task.execute();
        }
    }

    @Override
    public void close() {
        if (this.commandChannel != null) {
            this.commandChannel.close();
            this.commandChannel = null;
        }
    }

    @RequiredArgsConstructor
    public static class HelloMessage implements NetworkMessage {
        private final String hello;

        public static class Type implements NetworkMessageType<HelloMessage> {
            @Override
            public int getTypeId() {
                return 0;
            }

            @Override
            public int getSizeInBytes() {
                return 32;
            }

            @Override
            public Class<HelloMessage> getMessageClass() {
                return HelloMessage.class;
            }

            @Override
            public HelloMessage deserialize(final ByteBuffer bufferIn) {
                val bytes = new byte[32];
                for (int i = 0; i < 32; ++i) {
                    bytes[i] = bufferIn.get();
                }
                return new HelloMessage(new String(bytes, StandardCharsets.UTF_8));
            }

            @Override
            public void serialize(final HelloMessage message, final ByteBuffer bufferOut) {
                val bytes = message.hello.getBytes(StandardCharsets.UTF_8);
                val outBytes = new byte[32];
                for (int i = 0; i < bytes.length && i < 31; ++i) {
                    outBytes[i] = bytes[i];
                }

                bufferOut.put(outBytes);
            }

            @Override
            public void handle(
                    final HelloMessage message,
                    final MessageHandlingContext context
            ) {
                LOG.info("Received HelloMessage: {}", message.hello);
                context.getMainThread().accept(() -> LOG.info("On main thread?"));
            }
        }
    }
}
