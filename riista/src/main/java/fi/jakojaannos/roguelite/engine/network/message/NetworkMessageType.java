package fi.jakojaannos.roguelite.engine.network.message;

import java.nio.ByteBuffer;

// TODO: Create concept of "channels" here, so that message types can determine if they want to use
//  CommandChannel or UnreliableChannel

public interface NetworkMessageType<TMessage extends NetworkMessage> {
    /**
     * Message-type unique identifier. Must have same value on clients and the server for packets to be handled
     * correctly. Used to identify what type a packet is.
     *
     * @return unique id of this packet type
     */
    int getTypeId();

    Class<TMessage> getMessageClass();

    TMessage deserialize(ByteBuffer bufferIn);

    void serialize(TMessage message, ByteBuffer buffer);
}
