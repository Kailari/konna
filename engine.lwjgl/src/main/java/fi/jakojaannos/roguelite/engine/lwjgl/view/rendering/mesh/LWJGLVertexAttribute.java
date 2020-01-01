package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh;

import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

@RequiredArgsConstructor
public class LWJGLVertexAttribute implements VertexAttribute {
    private final Type type;
    private final int count;
    private final boolean normalized;

    public int getSizeInBytes() {
        return LWJGLType.forType(this.type).getSizeInBytes() * this.count;
    }

    public void apply(
            final int index,
            final int offset,
            final int stride
    ) {
        glEnableVertexAttribArray(index);
        glVertexAttribPointer(index,
                              this.count,
                              LWJGLType.forType(this.type).getGlType(),
                              this.normalized,
                              stride,
                              offset);
    }

    public enum LWJGLType {
        FLOAT(VertexAttribute.Type.FLOAT, GL_FLOAT, 4),
        DOUBLE(VertexAttribute.Type.DOUBLE, GL_DOUBLE, 8),
        INT(VertexAttribute.Type.INT, GL_INT, 8),
        UNSIGNED_INT(VertexAttribute.Type.UNSIGNED_INT, GL_UNSIGNED_INT, 8),
        BYTE(VertexAttribute.Type.BYTE, GL_BYTE, 8),
        UNSIGNED_BYTE(VertexAttribute.Type.UNSIGNED_BYTE, GL_UNSIGNED_BYTE, 8),
        SHORT(VertexAttribute.Type.SHORT, GL_SHORT, 8),
        UNSIGNED_SHORT(VertexAttribute.Type.UNSIGNED_SHORT, GL_UNSIGNED_SHORT, 8);

        @Getter private final Type type;
        @Getter private final int glType;
        @Getter private final int sizeInBytes;

        private static final Map<Type, LWJGLType> typeMappings = Arrays.stream(LWJGLType.values())
                                                                       .collect(Collectors.toMap(LWJGLType::getType,
                                                                                                 lwjglType -> lwjglType));

        LWJGLType(
                final Type type,
                final int glType,
                int sizeInBytes
        ) {
            this.type = type;
            this.glType = glType;
            this.sizeInBytes = sizeInBytes;
        }

        public static LWJGLType forType(final Type type) {
            return typeMappings.get(type);
        }
    }
}
