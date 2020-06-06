package fi.jakojaannos.roguelite.engine.network.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.network.message.NetworkMessage;
import fi.jakojaannos.roguelite.engine.network.message.NetworkMessageHandlerMap;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageDecoder;
import fi.jakojaannos.roguelite.engine.network.message.serialization.MessageEncoder;
import fi.jakojaannos.roguelite.engine.network.message.serialization.TypedNetworkMessage;

public abstract class CommandChannelRunnable implements Runnable, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(CommandChannelRunnable.class);

    protected final NetworkMessageHandlerMap messageHandlers;
    protected final Selector selector;
    private final MessageEncoder messageEncoder;
    private final MessageDecoder messageDecoder;
    private final Object writeLock = new Object();

    public boolean isConnected() {
        return this.selector != null && this.selector.isOpen();
    }

    protected CommandChannelRunnable(
            final NetworkMessageHandlerMap messageHandlers,
            final MessageEncoder messageEncoder,
            final MessageDecoder messageDecoder
    ) throws IOException {
        this.messageHandlers = messageHandlers;
        this.messageEncoder = messageEncoder;
        this.messageDecoder = messageDecoder;

        LOG.trace("Opening socket selector...");
        this.selector = Selector.open();
    }

    @Override
    public void run() {
        try {
            while (isConnected()) {
                handleReceivedMessages();

                final int selectedKeyCount = this.selector.select();
                if (selectedKeyCount > 0) {
                    final var keys = this.selector.selectedKeys();

                    final var keyIterator = keys.iterator();
                    while (keyIterator.hasNext()) {
                        final var selectionKey = keyIterator.next();
                        keyIterator.remove();
                        handleSelectedKey(this.selector, selectionKey);
                    }
                }
            }
        } catch (final IOException e) {
            LOG.error("Command channel thread has crashed:", e);
        } finally {
            shutdown();
        }
    }

    protected void handleSelectedKey(
            final Selector selector,
            final SelectionKey selectionKey
    ) throws IOException {
        if (selectionKey.isReadable()) {
            handleReadableKey(selectionKey);
        } else if (selectionKey.isWritable()) {
            handleWritableKey(selectionKey);
        }
    }

    protected void handleWritableKey(final SelectionKey selectionKey) throws IOException {
        synchronized (this.writeLock) {
            tryWriteMessageToChannel((SocketChannel) selectionKey.channel(),
                                     resolveWriteBufferFromKey(selectionKey));
        }
    }

    protected void handleReadableKey(
            final SelectionKey selectionKey
    ) throws IOException {
        final var channel = (SocketChannel) selectionKey.channel();

        final ByteBuffer readBuffer = resolveReadBufferFromKey(selectionKey);
        final Consumer<TypedNetworkMessage<?>> messageConsumer = resolveMessageConsumerFromKey(selectionKey);

        // FIXME: Connection reset (client loses connection) here crashes the server
        final var readCount = channel.read(readBuffer);
        final var endOfStream = readCount < 0;
        if (!endOfStream) {
            LOG.trace("Read {} bytes!", readCount);
            tryReadMessage(readBuffer, messageConsumer);
        } else {
            handleEndOfStream(selectionKey, channel);
        }
    }

    protected abstract ByteBuffer resolveWriteBufferFromKey(SelectionKey selectionKey);

    protected abstract ByteBuffer resolveReadBufferFromKey(SelectionKey selectionKey);

    protected abstract Consumer<TypedNetworkMessage<?>> resolveMessageConsumerFromKey(SelectionKey selectionKey);

    protected void tryReadMessage(
            final ByteBuffer readBuffer,
            final Consumer<TypedNetworkMessage<?>> messageConsumer
    ) {
        LOG.trace("Reading received message(s)...");
        readBuffer.flip();
        Optional<TypedNetworkMessage<?>> decodedMessage;
        do {
            decodedMessage = this.messageDecoder.decodeFromBuffer(readBuffer);
            decodedMessage.ifPresent(messageConsumer);
        }
        while (decodedMessage.isPresent());

        readBuffer.compact();
    }

    protected void shutdown() {
        if (this.selector.isOpen()) {
            try {
                this.selector.close();
            } catch (final IOException e) {
                LOG.error("Error closing selector:", e);
            }
        }
    }

    protected abstract void handleReceivedMessages();

    protected void handleEndOfStream(
            final SelectionKey selectionKey,
            final SocketChannel channel
    ) throws IOException {
        selectionKey.cancel();
        channel.close();
    }

    public void disconnect() {
        if (isConnected()) {
            LOG.trace("Initiating disconnect...");
        }

        if (this.selector != null) {
            try {
                this.selector.close();
            } catch (final IOException ignored) {
            }
        }
    }

    protected void encodeAndWrite(
            final NetworkMessage message,
            final ByteBuffer writeBuffer,
            final SocketChannel channel
    ) {
        synchronized (this.writeLock) {
            this.messageEncoder.encodeToBuffer(message, writeBuffer);

            try {
                tryWriteMessageToChannel(channel, writeBuffer);
            } catch (final IOException e) {
                LOG.error("Write to channel failed:", e);
            }
        }
    }

    protected void tryWriteMessageToChannel(
            final SocketChannel channel,
            final ByteBuffer writeBuffer
    ) throws IOException {
        writeBuffer.flip();
        if (writeBuffer.hasRemaining()) {
            LOG.info("Writing {} bytes to {}", writeBuffer.remaining(), channel.getRemoteAddress());

            final var remaining = writeBuffer.remaining();
            if (channel.write(writeBuffer) < remaining) {
                LOG.trace("Could not write the whole message. Registering for OP_WRITE");

                writeBuffer.compact();
                channel.keyFor(this.selector)
                       .interestOpsOr(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            } else {
                LOG.warn("Full write. Clearing buffer.");
                channel.keyFor(this.selector).interestOpsAnd(SelectionKey.OP_READ);
                writeBuffer.clear();
            }
        } else {
            LOG.warn("Buffer was empty! Clearing!");
            writeBuffer.clear();
        }
    }

    @Override
    public void close() {
        disconnect();
    }
}
