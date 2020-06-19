package fi.jakojaannos.roguelite.engine.network.message;

public interface NetworkMessage {
    NetworkMessageTypeMap TYPES = NetworkMessageTypeMap.builder()
                                                       .type(new HelloMessage.Type())
                                                       .build();
}
