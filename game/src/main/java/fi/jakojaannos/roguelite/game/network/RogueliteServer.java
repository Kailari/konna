package fi.jakojaannos.roguelite.game.network;

import fi.jakojaannos.roguelite.game.LogCategories;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

@Slf4j
@RequiredArgsConstructor
public class RogueliteServer {
    public static void run(final int port) throws Exception {
        LOG.info("Opening a server socket channel...");
        val connectionChannel = ServerSocketChannel.open();

        LOG.info("Binding to port {}", port);
        connectionChannel.bind(new InetSocketAddress(port));

        LOG.info("Configuring connection channel...");
        connectionChannel.configureBlocking(false);
        connectionChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

        LOG.info("Opening a socket selector...");
        Selector selector = Selector.open();
        connectionChannel.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer buffer = ByteBuffer.allocate(256);
        while (true) {
            int channelCount = selector.select();
            if (channelCount > 0) {
                val keys = selector.selectedKeys();

                val keyIterator = keys.iterator();
                while (keyIterator.hasNext()) {
                    val selectionKey = keyIterator.next();
                    keyIterator.remove();

                    if (selectionKey.isAcceptable()) {
                        val clientReadChannel = connectionChannel.accept();
                        LOG.info(LogCategories.NET_CONNECTION, "New connection from {}", clientReadChannel.getRemoteAddress());

                        clientReadChannel.configureBlocking(false);
                        clientReadChannel.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()) {
                        val clientReadChannel = (SocketChannel) selectionKey.channel();
                        if (clientReadChannel.read(buffer) < 0) {
                            LOG.info(LogCategories.NET_CONNECTION, "Client {} stream has reached EOS. Closing connection.", clientReadChannel.getRemoteAddress());
                            selectionKey.cancel();
                            clientReadChannel.close();
                        } else {
                            LOG.info("Echoing back a message.");
                            buffer.flip();
                            clientReadChannel.write(buffer);
                            buffer.clear();
                        }
                    }
                }
            }
        }
    }
}
