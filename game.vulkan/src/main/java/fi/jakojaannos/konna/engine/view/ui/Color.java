package fi.jakojaannos.konna.engine.view.ui;

import java.nio.ByteBuffer;

public record Color(float r, float g, float b, float a) {
    private static final int OFFSET_R = 0;
    private static final int OFFSET_G = Float.BYTES;
    private static final int OFFSET_B = 2 * Float.BYTES;
    private static final int OFFSET_A = 3 * Float.BYTES;

    public Color(final float r, final float g, final float b) {
        this(r, g, b, 1.0f);
    }

    public void getRGBA(final int offset, final ByteBuffer buffer) {
        buffer.putFloat(offset + OFFSET_R, this.r);
        buffer.putFloat(offset + OFFSET_G, this.g);
        buffer.putFloat(offset + OFFSET_B, this.b);
        buffer.putFloat(offset + OFFSET_A, this.a);
    }
}
