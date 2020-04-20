package fi.jakojaannos.roguelite.game.test.view;

import java.nio.file.Path;
import java.util.Optional;

import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.Window;
import fi.jakojaannos.roguelite.engine.view.audio.AudioContext;
import fi.jakojaannos.roguelite.engine.view.audio.SoundEffect;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormatBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.text.TextRenderer;

import static org.mockito.Mockito.mock;

public class TestRenderingBackend implements RenderingBackend {
    @Override
    public TextRenderer getTextRenderer(
    ) {
        return mock(TextRenderer.class);
    }

    @Override
    public Viewport getViewport(final Window window) {
        return new Viewport(window.getWidth(), window.getHeight());
    }

    @Override
    public Camera createCamera(final Viewport viewport) {
        return new TestCamera(viewport);
    }

    @Override
    public SpriteBatch createSpriteBatch(
            final Path assetRoot,
            final String shader
    ) {
        return mock(SpriteBatch.class);
    }

    @Override
    public Mesh createMesh(final VertexFormat vertexFormat) {
        return mock(Mesh.class);
    }

    @Override
    public VertexFormatBuilder createVertexFormat() {
        return new TestVertexFormatBuilder();
    }

    @Override
    public ShaderBuilder createShaderProgram() {
        return new TestShaderBuilder();
    }

    @Override
    public AudioContext createAudioContext() {
        return new AudioContext() {
            @Override
            public Optional<Integer> nextSource(final int priority) {
                return Optional.empty();
            }

            @Override
            public SoundEffect createEffect(
                    final Path assetRoot,
                    final String filename
            ) {
                return mock(SoundEffect.class);
            }

            @Override
            public void close() {
            }
        };
    }

    @Override
    public void close() {
    }
}
