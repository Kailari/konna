package fi.jakojaannos.roguelite.game.network;

import fi.jakojaannos.roguelite.game.network.client.RogueliteClient;
import fi.jakojaannos.roguelite.game.network.message.MessageHandlingContext;
import fi.jakojaannos.roguelite.game.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.game.network.message.NetworkMessageType;
import fi.jakojaannos.roguelite.game.network.message.NetworkMessageTypeMap;
import fi.jakojaannos.roguelite.game.network.message.serialization.NetworkMessageDecoder;
import fi.jakojaannos.roguelite.game.network.message.serialization.TypedNetworkMessage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.function.Consumer;

// FIXME: no reason to use NIO sockets here unless this thing is abstracted to handle server too
//  either refactor to regular blocking sockets or abstract the hell out of everything

@Slf4j
public class CommandChannel implements AutoCloseable {
    private final ClientCommandChannelThread networkThread;

    private final Queue<MainThreadTask> inboundTaskQueue = new ArrayDeque<>();
    private final NetworkMessageTypeMap typeMap = NetworkMessageTypeMap.builder()
                                                                       .messageType(new RogueliteClient.HelloMessage.Type())
                                                                       .build();


    public CommandChannel(final String host, final int port) {
        LOG.trace("Starting network thread...");
        this.networkThread = new ClientCommandChannelThread(host, port, typeMap, this::queueTaskOnMainThread);

        try {
            synchronized (this.networkThread.readyLock()) {
                new Thread(this.networkThread, "Network").start();
                this.networkThread.readyLock().wait();
            }
        } catch (InterruptedException ignored) {
        }
    }

    private void queueTaskOnMainThread(final MainThreadTask mainThreadTask) {
        synchronized (this.inboundTaskQueue) {
            this.inboundTaskQueue.offer(mainThreadTask);
        }
    }

    public Collection<MainThreadTask> pollMessageTasks() {
        List<MainThreadTask> result;
        synchronized (this.inboundTaskQueue) {
            // FIXME: Instead of buffer copy & clear, use double buffering
            result = List.copyOf(this.inboundTaskQueue);
            this.inboundTaskQueue.clear();
        }

        return result;
    }

    public void send(final NetworkMessage message) {
        this.networkThread.queueWrite(message);
    }

    @Override
    public void close() {
        this.networkThread.disconnect();
    }

    public static class ClientCommandChannelThread implements Runnable {
        private final Queue<NetworkMessage> writeQueue = new ArrayDeque<>();
        private final ByteBuffer readBuffer, writeBuffer;

        private final String host;
        private final int port;
        private final NetworkMessageTypeMap typeMap;
        private final Consumer<MainThreadTask> mainThreadTaskAcceptor;

        @Nullable private SocketChannel channel;
        @Nullable private Selector selector;

        private boolean shouldClose;
        private final Object readyLock = new Object();

        public ClientCommandChannelThread(
                final String host,
                final int port,
                final NetworkMessageTypeMap typeMap,
                final Consumer<MainThreadTask> mainThreadTaskAcceptor
        ) {
            this.host = host;
            this.port = port;
            this.typeMap = typeMap;
            this.mainThreadTaskAcceptor = mainThreadTaskAcceptor;

            this.readBuffer = ByteBuffer.allocate(2048);
            this.writeBuffer = ByteBuffer.allocate(2048);
        }

        @Override
        public void run() {
            try {
                synchronized (this.readyLock()) {
                    // Start the client.
                    LOG.trace("Connecting TCP channel to {}:{}", this.host, this.port);
                    this.channel = SocketChannel.open();
                    channel.configureBlocking(false);
                    channel.socket().setKeepAlive(true);
                    channel.connect(new InetSocketAddress(this.host, this.port));

                    try {
                        channel.finishConnect();
                    } catch (ConnectException e) {
                        LOG.error("Could not connect: {}", e.getMessage());
                        this.readyLock().notifyAll();
                        return;
                    }

                    LOG.trace("TCP channel connected.");

                    this.selector = Selector.open();
                    channel.register(selector, SelectionKey.OP_READ);

                    this.readyLock.notifyAll();
                }

                val decoder = new NetworkMessageDecoder(this.typeMap);
                val context = new MessageHandlingContext(this.mainThreadTaskAcceptor);
                List<TypedNetworkMessage<?>> receiveQueue = new ArrayList<>();
                while (channel.isConnected() && !this.shouldClose) {
                    assert selector != null;
                    int channelCount = selector.select();
                    if (channelCount > 0) {
                        val keys = selector.selectedKeys();

                        val keyIterator = keys.iterator();
                        while (keyIterator.hasNext()) {
                            val selectionKey = keyIterator.next();
                            keyIterator.remove();

                            if (selectionKey.isReadable()) {
                                LOG.trace("Reading received message...");
                                val readChannel = (SocketChannel) selectionKey.channel();
                                readChannel.read(this.readBuffer);
                                this.readBuffer.flip();

                                //noinspection StatementWithEmptyBody
                                while (decoder.decode(this.readBuffer, receiveQueue::add)) {}

                                this.readBuffer.compact();
                            }
                        }

                    }

                    flush();

                    for (val message : receiveQueue) {
                        LOG.info("Handling received message!");
                        message.handle(context);
                    }
                    receiveQueue.clear();
                }

                LOG.trace("Network thread has exited the loop.");

                if (this.channel.isConnected()) {
                    this.channel.close();
                }
                if (this.selector != null && this.selector.isOpen()) {
                    selector.close();
                }
            } catch (IOException e) {
                LOG.error("Socket listener has crashed!", e);
            }
        }

        public void queueWrite(final NetworkMessage message) {
            synchronized (this.writeQueue) {
                this.writeQueue.offer(message);
            }

            if (this.selector != null) {
                this.selector.wakeup();
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void flush() {
            if (this.channel == null) {
                LOG.warn("Tried flushing while channel was null!");
                return;
            }
            // TODO: Use double buffering to avoid stalling both threads
            synchronized (this.writeQueue) {
                while (!this.writeQueue.isEmpty()) {
                    val message = this.writeQueue.poll();

                    NetworkMessageType messageType = this.typeMap.getByMessageClass((Class<NetworkMessage>) message.getClass()).orElseThrow();
                    TypedNetworkMessage.forWriting(messageType, message)
                                       .write(this.writeBuffer);
                }
            }

            try {
                this.writeBuffer.flip();
                this.channel.write(this.writeBuffer);
                this.writeBuffer.clear();
            } catch (IOException e) {
                LOG.error("Error flushing the CommandChannel:", e);
            }
        }

        public void disconnect() {
            LOG.trace("Initiating disconnect...");
            this.shouldClose = true;
            if (this.selector != null) {
                this.selector.wakeup();
            }
        }

        public Object readyLock() {
            return this.readyLock;
        }
    }
}
