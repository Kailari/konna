package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class LWJGLVertexAttribute implements VertexAttribute {
    private final Type type;
    private final int count;
    private final boolean normalized;

    private final boolean instanced;

    @Override
    public boolean isInstanced() {
        return this.instanced;
    }

    public int getSizeInBytes() {
        return LWJGLType.forType(this.type).getSizeInBytes() * this.count;
    }

    public LWJGLVertexAttribute(
            final boolean instanced,
            final Type type,
            final int count,
            final boolean normalized
    ) {
        this.instanced = instanced;
        this.type = type;
        this.count = count;
        this.normalized = normalized;
    }

    public void apply(final int index, final int offset, final int stride) {
        glEnableVertexAttribArray(index);
        glVertexAttribPointer(index,
                              this.count,
                              LWJGLType.forType(this.type).getGlType(),
                              this.normalized,
                              stride,
                              offset);

        if (isInstanced()) {
            glVertexAttribDivisor(index, 1);
        }
    }

    public enum LWJGLType {
        FLOAT(VertexAttribute.Type.FLOAT, GL_FLOAT, 4),
        DOUBLE(VertexAttribute.Type.DOUBLE, GL_DOUBLE, 8),
        INT(VertexAttribute.Type.INT, GL_INT, 4),
        UNSIGNED_INT(VertexAttribute.Type.UNSIGNED_INT, GL_UNSIGNED_INT, 4),
        BYTE(VertexAttribute.Type.BYTE, GL_BYTE, 1),
        UNSIGNED_BYTE(VertexAttribute.Type.UNSIGNED_BYTE, GL_UNSIGNED_BYTE, 1),
        SHORT(VertexAttribute.Type.SHORT, GL_SHORT, 2),
        UNSIGNED_SHORT(VertexAttribute.Type.UNSIGNED_SHORT, GL_UNSIGNED_SHORT, 2);

        private static final Map<Type, LWJGLType> typeMappings = Arrays.stream(LWJGLType.values())
                                                                       .collect(Collectors.toMap(LWJGLType::getType, lwjglType -> lwjglType));
        private final Type type;
        private final int glType;
        private final int sizeInBytes;

        public Type getType() {
            return this.type;
        }

        public int getGlType() {
            return this.glType;
        }

        public int getSizeInBytes() {
            return this.sizeInBytes;
        }

        LWJGLType(final Type type, final int glType, final int sizeInBytes) {
            this.type = type;
            this.glType = glType;
            this.sizeInBytes = sizeInBytes;
        }

        public static LWJGLType forType(final Type type) {
            return typeMappings.get(type);
        }
    }
}
