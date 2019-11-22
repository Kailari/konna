package fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;

import com.google.gson.*;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.view.rendering.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatchBase;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
public class LWJGLSpriteBatch extends SpriteBatchBase<String, LWJGLCamera, LWJGLTexture> {
    private static final Matrix4f DEFAULT_TRANSFORM = new Matrix4f().identity();
    private static final int MAX_SPRITES_PER_BATCH = 4096;
    private static final int VERTICES_PER_SPRITE = 4;
    private static final int SIZE_IN_BYTES = (2 + 2 + 3) * 4;

    private final TextureRegion<LWJGLTexture> defaultTextureRegion;

    private final ShaderProgram shader;
    private final int uniformProjectionMatrix;
    private final int uniformViewMatrix;
    private final int uniformModelMatrix;

    private final int vao;
    private final int vbo;
    private final int ebo;

    private ByteBuffer vertexDataBuffer;
    private Map<String, LWJGLTexture> textures = new HashMap<>();
    private Map<String, Sprite<LWJGLTexture>> sprites = new HashMap<>();
    private Path assetRoot;

    public LWJGLSpriteBatch(
            @NonNull String assetRoot,
            @NonNull String shader
    ) {
        super(MAX_SPRITES_PER_BATCH);
        this.assetRoot = Paths.get(assetRoot);
        this.defaultTextureRegion = new TextureRegion<>(new LWJGLTexture(this.assetRoot, "textures/sheep.png"), 0, 0, 1, 1);
        this.shader = createShader(assetRoot, shader);
        this.uniformModelMatrix = this.shader.getUniformLocation("model");
        this.uniformViewMatrix = this.shader.getUniformLocation("view");
        this.uniformProjectionMatrix = this.shader.getUniformLocation("projection");

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        this.vbo = glGenBuffers();
        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        val indices = constructIndicesArray();
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        this.vertexDataBuffer = MemoryUtil.memAlloc(MAX_SPRITES_PER_BATCH * VERTICES_PER_SPRITE * SIZE_IN_BYTES);
        for (int i = 0; i < MAX_SPRITES_PER_BATCH * VERTICES_PER_SPRITE; ++i) {
            updateVertex(i * SIZE_IN_BYTES, 0, 0, 0, 0, 0, 0, 0);
        }

    }

    @Override
    public TextureRegion<LWJGLTexture> resolveTexture(@NonNull String spriteName, int frame) {
        return this.sprites.computeIfAbsent(spriteName,
                                            key -> loadSprite(spriteName))
                           .getFrameOrWhole(frame);
    }

    private Sprite<LWJGLTexture> loadSprite(@NonNull String spriteName) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Sprite.class, (JsonDeserializer<Sprite<LWJGLTexture>>) this::deserializeSprite)
                .create();
        try (val reader = new InputStreamReader(Files.newInputStream(assetRoot.resolve(spriteName + ".json"), StandardOpenOption.READ))) {
            // noinspection unchecked
            return gson.fromJson(reader, Sprite.class);
        } catch (IOException e) {
            LOG.error("Reading sprite \"{}\" failed!", spriteName);
            LOG.error("Exception: ", e);
            return new Sprite<>(List.of(this.defaultTextureRegion));
        }
    }

    public Sprite<LWJGLTexture> deserializeSprite(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context
    ) throws JsonParseException {
        val jsonObject = json.getAsJsonObject();
        val framesJson = jsonObject.getAsJsonArray("frames");

        val frames = new ArrayList<TextureRegion<LWJGLTexture>>();
        for (val frameElement : framesJson) {
            val frameJson = frameElement.getAsJsonObject();
            val texturePath = frameJson.get("texture").getAsString();
            val texture = this.textures.computeIfAbsent(texturePath,
                                                        key -> new LWJGLTexture(this.assetRoot, texturePath));
            val x = frameJson.get("x").getAsDouble();
            val y = frameJson.get("y").getAsDouble();
            val w = frameJson.get("w").getAsDouble();
            val h = frameJson.get("h").getAsDouble();
            val u0 = x / texture.getWidth();
            val v0 = y / texture.getHeight();
            val u1 = u0 + w / texture.getWidth();
            val v1 = v0 + h / texture.getHeight();
            frames.add(new TextureRegion<>(texture, u0, v0, u1, v1));
        }

        return new Sprite<>(frames);
    }

    @Override
    protected void queueFrame(
            @NonNull TextureRegion<LWJGLTexture> region,
            double x,
            double y,
            double width,
            double height
    ) {
        val offset = getNFrames() * VERTICES_PER_SPRITE * SIZE_IN_BYTES;
        updateVertex(offset,
                     x, y,
                     (float) region.getU0(), (float) region.getV0(),
                     1.0f, 1.0f, 1.0f);
        updateVertex(offset + SIZE_IN_BYTES,
                     x + width, y,
                     (float) region.getU1(), (float) region.getV0(),
                     1.0f, 1.0f, 1.0f);
        updateVertex(offset + (2 * SIZE_IN_BYTES),
                     x + width, y + height,
                     (float) region.getU1(), (float) region.getV1(),
                     1.0f, 1.0f, 1.0f);
        updateVertex(offset + (3 * SIZE_IN_BYTES),
                     x, y + height,
                     (float) region.getU0(), (float) region.getV1(),
                     1.0f, 1.0f, 1.0f);
        /*updateVertex(offset,
                     x, y,
                     0.0f, 0.0f,
                     1.0f, 1.0f, 1.0f);
        updateVertex(offset + SIZE_IN_BYTES,
                     x + width, y,
                     1.0f, 0.0f,
                     1.0f, 1.0f, 1.0f);
        updateVertex(offset + (2 * SIZE_IN_BYTES),
                     x + width, y + height,
                     1.0f, 1.0f,
                     1.0f, 1.0f, 1.0f);
        updateVertex(offset + (3 * SIZE_IN_BYTES),
                     x, y + height,
                     0.0f, 1.0f,
                     1.0f, 1.0f, 1.0f);*/
    }

    private void updateVertex(
            int offset,
            double x,
            double y,
            double u,
            double v,
            float r,
            float g,
            float b
    ) {
        this.vertexDataBuffer.putFloat(offset, (float) x);
        this.vertexDataBuffer.putFloat(offset + 4, (float) y);
        this.vertexDataBuffer.putFloat(offset + 8, (float) u);
        this.vertexDataBuffer.putFloat(offset + 12, (float) v);
        this.vertexDataBuffer.putFloat(offset + 16, r);
        this.vertexDataBuffer.putFloat(offset + 20, g);
        this.vertexDataBuffer.putFloat(offset + 24, b);
    }

    @Override
    protected void flush(
            @NonNull LWJGLTexture texture,
            @NonNull LWJGLCamera camera,
            Matrix4f transformation
    ) {
        this.shader.use();
        texture.bind();

        this.shader.setUniformMat4x4(this.uniformProjectionMatrix, camera.getProjectionMatrix());
        this.shader.setUniformMat4x4(this.uniformViewMatrix, camera.getViewMatrix());
        this.shader.setUniformMat4x4(this.uniformModelMatrix,
                                     transformation != null
                                             ? transformation
                                             : DEFAULT_TRANSFORM);

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        this.vertexDataBuffer.limit(getNFrames() * VERTICES_PER_SPRITE * SIZE_IN_BYTES);
        // FIXME: BufferSubData causes segfault in native code
        //glBufferSubData(GL_ARRAY_BUFFER, 0, this.vertexDataBuffer);
        glBufferData(GL_ARRAY_BUFFER, this.vertexDataBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0,
                              2,
                              GL_FLOAT,
                              false,
                              SIZE_IN_BYTES,
                              0); // offset: pos is first attribute -> 0
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1,
                              2,
                              GL_FLOAT,
                              false,
                              SIZE_IN_BYTES,
                              2 * 4); // offset: pos = 2 * sizeof(float)
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2,
                              3,
                              GL_FLOAT,
                              false,
                              SIZE_IN_BYTES,
                              4 * 4); // offset: pos + uv = 4 * sizeof(float)

        glDrawElements(GL_TRIANGLES,
                       getNFrames() * 6,
                       GL_UNSIGNED_INT,
                       NULL);

        this.vertexDataBuffer.limit(this.vertexDataBuffer.capacity());
    }

    @Override
    public void close() {
        MemoryUtil.memFree(this.vertexDataBuffer);
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);

        this.shader.close();
        this.sprites.clear();
        this.textures.values().forEach(LWJGLTexture::close);
    }

    private int[] constructIndicesArray() {
        val indices = new int[MAX_SPRITES_PER_BATCH * 6];
        for (int i = 0, j = 0; i < indices.length; i += 6, j += 4) {
            indices[i + 0] = j + 0;
            indices[i + 1] = j + 1;
            indices[i + 2] = j + 2;
            indices[i + 3] = j + 2;
            indices[i + 4] = j + 3;
            indices[i + 5] = j + 0;
        }
        return indices;
    }

    private static ShaderProgram createShader(String assetRoot, String shader) {
        return new ShaderProgram(
                Paths.get(assetRoot, "shaders", shader + ".vert").toString(),
                Paths.get(assetRoot, "shaders", shader + ".frag").toString(),
                Map.ofEntries(
                        Map.entry(0, "in_pos"),
                        Map.entry(1, "in_uv"),
                        Map.entry(2, "in_tint")
                ),
                Map.ofEntries(
                        Map.entry(0, "out_fragColor")
                )
        );
    }
}
