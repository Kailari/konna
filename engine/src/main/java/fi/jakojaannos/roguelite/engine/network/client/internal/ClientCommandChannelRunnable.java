package fi.jakojaannos.roguelite.engine.network.client.internal;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.network.NetworkConnection;
import fi.jakojaannos.roguelite.engine.network.internal.CommandChannelRunnable;
import fi.jakojaannos.roguelite.engine.network.message.MessageHandlingContext;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessageHandlerMap;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageDecoder;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageEncoder;
import fi.jakojaannos.roguelite.engine.network.message.serialization.TypedNetworkMessage;

@Slf4j
public class ClientCommandChannelRunnable extends CommandChannelRunnable {
    protected final ByteBuffer readBuffer;
    protected final ByteBuffer writeBuffer;
    private final Queue<TypedNetworkMessage<?>> receiveQueue = new ArrayDeque<>();
    private final MessageHandlingContext context;
    @Nullable protected SocketChannel channel;

    public ClientCommandChannelRunnable(
            final String host,
            final int port,
            final NetworkMessageHandlerMap messageHandlers,
            final MessageEncoder messageEncoder,
            final MessageDecoder messageDecoder,
            final MainThread mainThread
    ) throws IOException {
        super(messageHandlers, messageEncoder, messageDecoder);
        LOG.info("Connecting to {}:{}", host, port);

        this.readBuffer = ByteBuffer.allocate(2048);
        this.writeBuffer = ByteBuffer.allocate(2048);
        this.context = new MessageHandlingContext(mainThread, new NetworkConnection(-1));

        LOG.trace("Connecting TCP channel to {}:{}", host, port);
        this.channel = SocketChannel.open();
        this.channel.configureBlocking(false);
        this.channel.socket().setKeepAlive(true);
        this.channel.connect(new InetSocketAddress(host, port));

        try {
            this.channel.finishConnect();
        } catch (ConnectException e) {
            LOG.warn("Could not connect: {}", e.getMessage());
            return;
        }

        LOG.info("Connection successful.");
        this.channel.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() && this.channel != null && this.channel.isConnected();
    }

    public void send(final NetworkMessage message) {
        if (this.isConnected()) {
            encodeAndWrite(message, this.writeBuffer, this.channel);
        }
    }

    @Override
    protected void handleReceivedMessages() {
        synchronized (this.receiveQueue) {
            while (!this.receiveQueue.isEmpty()) {
                this.messageHandlers.tryHandle(this.receiveQueue.poll(), this.context);
            }
        }
    }

    @Override
    protected ByteBuffer resolveWriteBufferFromKey(final SelectionKey selectionKey) {
        return this.writeBuffer;
    }

    @Override
    protected ByteBuffer resolveReadBufferFromKey(final SelectionKey selectionKey) {
        return this.readBuffer;
    }

    @Override
    protected Consumer<TypedNetworkMessage<?>> resolveMessageConsumerFromKey(final SelectionKey selectionKey) {
        return this.receiveQueue::offer;
    }

    @Override
    protected void shutdown() throws IOException {
        if (this.channel != null && this.channel.isConnected()) {
            this.channel.close();
        }
        super.shutdown();
    }

    @Override
    protected void handleEndOfStream(
            final SelectionKey selectionKey,
            final SocketChannel channel
    ) throws IOException {
        LOG.info("Connection input stream has reached EOS. Closing connection.");
        super.handleEndOfStream(selectionKey, channel);
    }
}
