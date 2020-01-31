package fi.jakojaannos.roguelite.engine.network.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.MainThread;
import fi.jakojaannos.roguelite.engine.network.NetworkConnection;
import fi.jakojaannos.roguelite.engine.network.message.MessageHandlingContext;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessageHandlerMap;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageDecoder;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageEncoder;
import fi.jakojaannos.roguelite.engine.network.message.serialization.TypedNetworkMessage;

@Slf4j
public class ServerCommandChannelRunnable extends CommandChannelRunnable {
    private final MainThread mainThread;
    private final Map<NetworkConnection, ClientInfo> clients = new HashMap<>();
    private final ServerSocketChannel connectionChannel;
    private int clientIdCounter;

    public ServerCommandChannelRunnable(
            final int port,
            final NetworkMessageHandlerMap messageHandlers,
            final MessageEncoder messageEncoder,
            final MessageDecoder messageDecoder,
            final MainThread mainThread
    ) throws IOException {
        super(messageHandlers, messageEncoder, messageDecoder);
        this.mainThread = mainThread;

        LOG.trace("Starting TCP channel for listening new connections on port {}", port);
        this.connectionChannel = ServerSocketChannel.open();

        this.connectionChannel.bind(new InetSocketAddress(port));
        this.connectionChannel.configureBlocking(false);

        this.connectionChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        this.clientIdCounter = 0;
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() && this.connectionChannel != null && this.connectionChannel.isOpen();
    }

    @Override
    protected void shutdown() {
        if (this.connectionChannel != null && this.connectionChannel.isOpen()) {
            try {
                this.connectionChannel.close();
            } catch (final IOException e) {
                LOG.error("Error closing connection channel:", e);
            }
        }
        super.shutdown();
    }

    public void send(final NetworkConnection target, final NetworkMessage message) {
        final ClientInfo clientInfo;
        synchronized (this.clients) {
            clientInfo = this.clients.get(target);

            if (clientInfo == null) {
                LOG.warn("Skipping write, client already disconnected!");
                return;
            }

            try {
                // Semaphore is used to make sure the selector thread cannot acquire lock on client
                // info before we have locked it down. If synchronized-blocks were used, we would
                // need to keep the clients locked until the end of the write.
                clientInfo.removalSemaphore.acquire();
            } catch (final InterruptedException e) {
                LOG.warn("Write was interrupted while waiting for client info semaphore!");
                return;
            }
        }

        try {
            encodeAndWrite(message,
                           clientInfo.writeBuffer,
                           clientInfo.channel);
        }
        // Making it this far means that we have acquired the semaphore. Ensure the semaphore is released.
        finally {
            clientInfo.removalSemaphore.release();
        }
    }

    @Override
    protected void handleSelectedKey(
            final Selector selector,
            final SelectionKey selectionKey
    ) throws IOException {
        if (selectionKey.isAcceptable()) {
            acceptConnection(selector, this.clientIdCounter);
        } else {
            super.handleSelectedKey(selector, selectionKey);
        }
    }

    @Override
    protected ByteBuffer resolveWriteBufferFromKey(final SelectionKey selectionKey) {
        final var connection = (NetworkConnection) selectionKey.attachment();
        final var clientInfo = this.clients.get(connection);
        return clientInfo.writeBuffer;
    }

    @Override
    protected ByteBuffer resolveReadBufferFromKey(final SelectionKey selectionKey) {
        final var clientConnection = (NetworkConnection) selectionKey.attachment();
        final var clientInfo = this.clients.get(clientConnection);
        return clientInfo.readBuffer;
    }

    @Override
    protected Consumer<TypedNetworkMessage<?>> resolveMessageConsumerFromKey(final SelectionKey selectionKey) {
        final var clientConnection = (NetworkConnection) selectionKey.attachment();
        final var clientInfo = this.clients.get(clientConnection);
        return clientInfo.receiveQueue::offer;
    }

    @Override
    protected void handleReceivedMessages() {
        this.clients.forEach((connection, clientInfo) -> {
            while (!clientInfo.receiveQueue.isEmpty()) {
                final var received = clientInfo.receiveQueue.remove();
                this.messageHandlers.tryHandle(received, clientInfo.context);
            }
        });
    }

    @Override
    protected void handleEndOfStream(
            final SelectionKey selectionKey,
            final SocketChannel channel
    ) throws IOException {
        LOG.info("Client {} stream has reached EOS. Closing connection.",
                 ((SocketChannel) selectionKey.channel()).getRemoteAddress());

        synchronized (this.clients) {
            final var connection = (NetworkConnection) selectionKey.attachment();
            final var clientInfo = this.clients.get(connection);
            try {
                clientInfo.removalSemaphore.acquire();

                this.clients.remove(connection);
            } catch (final InterruptedException e) {
                LOG.warn("Selector thread was interrupted while waiting for removal semaphore for removing a client!");
            } finally {
                clientInfo.removalSemaphore.release();
            }

            super.handleEndOfStream(selectionKey, channel);
        }
    }

    protected void acceptConnection(
            final Selector selector,
            final int clientId
    ) throws IOException {
        final var clientChannel = this.connectionChannel.accept();
        LOG.info("New connection from {}", clientChannel.getRemoteAddress());

        clientChannel.configureBlocking(false);

        final var clientConnection = new NetworkConnection(clientId);
        final var clientInfo = new ClientInfo(clientChannel,
                                              new MessageHandlingContext(this.mainThread, clientConnection));

        clientChannel.register(selector, SelectionKey.OP_READ, clientConnection);
        this.clients.put(clientConnection, clientInfo);
        this.clientIdCounter++;
    }

    @RequiredArgsConstructor
    private static class ClientInfo {
        private final Queue<TypedNetworkMessage<?>> receiveQueue = new ArrayDeque<>();
        private final ByteBuffer writeBuffer = ByteBuffer.allocate(2048);
        private final ByteBuffer readBuffer = ByteBuffer.allocate(2048);
        private final Semaphore removalSemaphore = new Semaphore(1);
        private final SocketChannel channel;
        private final MessageHandlingContext context;
    }
}
