package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

@RequiredArgsConstructor
public class VertexAttribute {
    private final Type type;
    private final int count;
    private final boolean normalized;

    public int getSizeInBytes() {
        return this.type.getSizeInBytes() * this.count;
    }

    public void apply(
            final int index,
            final int offset,
            final int stride
    ) {
        glEnableVertexAttribArray(index);
        glVertexAttribPointer(index,
                              this.count,
                              this.type.getGlType(),
                              this.normalized,
                              stride,
                              offset);
    }

    public enum Type {
        FLOAT(GL_FLOAT, 4),
        DOUBLE(GL_DOUBLE, 8),
        INT(GL_INT, 8),
        UNSIGNED_INT(GL_UNSIGNED_INT, 8),
        BYTE(GL_BYTE, 8),
        UNSIGNED_BYTE(GL_UNSIGNED_BYTE, 8),
        SHORT(GL_SHORT, 8),
        UNSIGNED_SHORT(GL_UNSIGNED_SHORT, 8);

        @Getter private final int glType;
        @Getter private final int sizeInBytes;

        Type(final int glType, int sizeInBytes) {
            this.glType = glType;
            this.sizeInBytes = sizeInBytes;
        }
    }
}
