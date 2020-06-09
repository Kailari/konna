package fi.jakojaannos.roguelite.engine.view;

import java.nio.ByteBuffer;

public interface EntityWriter {
    void write(ByteBuffer buffer, int offset);
}
