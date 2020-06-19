package fi.jakojaannos.roguelite.engine.network.message;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record HelloMessage(String hello) implements NetworkMessage {
    public static class Type implements NetworkMessageType<HelloMessage> {
        @Override
        public int getTypeId() {
            return 0;
        }

        @Override
        public Class<HelloMessage> getMessageClass() {
            return HelloMessage.class;
        }

        @Override
        public HelloMessage deserialize(final ByteBuffer bufferIn) {
            final var bytes = new byte[bufferIn.getInt()];
            for (int i = 0; i < bytes.length; ++i) {
                bytes[i] = bufferIn.get();
            }
            return new HelloMessage(new String(bytes, StandardCharsets.UTF_8));
        }

        @Override
        public void serialize(
                final HelloMessage message,
                final ByteBuffer bufferOut
        ) {
            final var bytes = message.hello.getBytes(StandardCharsets.UTF_8);
            bufferOut.putInt(bytes.length);
            bufferOut.put(bytes);
        }
    }
}
