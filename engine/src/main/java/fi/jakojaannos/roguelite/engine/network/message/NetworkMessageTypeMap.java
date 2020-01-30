package fi.jakojaannos.roguelite.engine.network.message;

import lombok.Builder;
import lombok.Singular;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NetworkMessageTypeMap {
    private final Map<Class<?>, NetworkMessageType<?>> messageTypesByClass;
    private final List<Optional<NetworkMessageType<?>>> messageTypesByTypeId;

    @Builder
    public NetworkMessageTypeMap(@Singular final Collection<NetworkMessageType<?>> types) {
        final var maxId = types.stream()
                               .mapToInt(NetworkMessageType::getTypeId)
                               .max()
                               .orElse(-1);
        this.messageTypesByTypeId = IntStream.rangeClosed(0, maxId)
                                             .mapToObj(typeId -> types.stream()
                                                                      .filter(type -> type.getTypeId() == typeId)
                                                                      .findFirst())
                                             .collect(Collectors.toUnmodifiableList());
        this.messageTypesByClass = types.stream()
                                        .collect(Collectors.toUnmodifiableMap(NetworkMessageType::getMessageClass,
                                                                              type -> type));
    }

    @SuppressWarnings("unchecked")
    public <TMessage extends NetworkMessage> Optional<NetworkMessageType<TMessage>> getByMessageClass(
            final Class<TMessage> messageClass
    ) {
        return Optional.ofNullable((NetworkMessageType<TMessage>) this.messageTypesByClass.get(messageClass));
    }

    public Optional<NetworkMessageType<?>> getByMessageTypeId(final int messageTypeId) {
        return this.messageTypesByTypeId.get(messageTypeId);
    }
}
