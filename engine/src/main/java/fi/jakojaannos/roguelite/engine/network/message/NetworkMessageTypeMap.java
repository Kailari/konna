package fi.jakojaannos.roguelite.engine.network.message;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NetworkMessageTypeMap {
    private final Map<Class<?>, NetworkMessageType<?>> messageTypesByClass;
    private final List<Optional<NetworkMessageType<?>>> messageTypesByTypeId;

    public static Builder builder() {
        return new Builder();
    }

    public NetworkMessageTypeMap(final Collection<NetworkMessageType<?>> types) {
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

    public static final class Builder {
        private final Collection<NetworkMessageType<?>> types = new ArrayList<>();

        public NetworkMessageTypeMap build() {
            return new NetworkMessageTypeMap(this.types);
        }

        public Builder type(final HelloMessage.Type type) {
            this.types.add(type);
            return this;
        }
    }
}
