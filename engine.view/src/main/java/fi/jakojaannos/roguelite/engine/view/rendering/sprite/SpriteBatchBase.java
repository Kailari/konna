package fi.jakojaannos.roguelite.engine.view.rendering.sprite;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;

@Slf4j
public abstract class SpriteBatchBase
        implements SpriteBatch {
    private static final double ROTATION_EPSILON = 0.0001;
    private final int maxFramesPerBatch;

    private boolean beginCalled;
    private Matrix4f activeTransformation;
    private Texture activeTexture;
    @Getter(AccessLevel.PROTECTED) private int nFrames;

    protected SpriteBatchBase(final int maxFramesPerBatch) {
        this.maxFramesPerBatch = maxFramesPerBatch;
    }

    protected abstract void queueFrameUnrotated(
            TextureRegion textureRegion,
            double x0,
            double y0,
            double x1,
            double y1,
            double r,
            double g,
            double b
    );

    /**
     * Flushes the currently queued sprites.
     *
     * @param texture        texture to use
     * @param transformation global transformations to apply
     */
    protected abstract void flush(
            Texture texture,
            @Nullable Matrix4f transformation
    );

    /**
     * Queues a new sprite animation frame for rendering. Passing in -1 as the frame renders the whole texture.
     *
     * @param texture texture to render
     * @param x       world x-coordinate to place the sprite to
     * @param y       world y-coordinate to place the sprite to
     * @param width   horizontal size of the sprite in world units
     * @param height  vertical size of the sprite in world units
     */
    protected abstract void queueFrame(
            TextureRegion texture,
            double x,
            double y,
            double originX,
            double originY,
            double width,
            double height,
            double rotation
    );

    @Override
    public void begin(@Nullable final Matrix4f transformation) {
        this.activeTransformation = transformation;
        if (this.beginCalled) {
            LOG.error("SpriteBatch.begin() called without calling .end() first!");
            return;
        }

        this.beginCalled = true;
    }

    @Override
    public void end() {
        if (!this.beginCalled) {
            LOG.error("SpriteBatch.end() called without calling .begin() first!");
            return;
        }
        if (this.nFrames > 0) {
            flush(this.activeTexture, this.activeTransformation);
        }
        this.nFrames = 0;
        this.activeTexture = null;
        this.activeTransformation = null;
        this.beginCalled = false;
    }

    @Override
    public void draw(
            final Sprite sprite,
            final String animation,
            final int frame,
            final double x,
            final double y,
            final double originX,
            final double originY,
            final double width,
            final double height,
            final double rotation
    ) {
        final var textureRegion = sprite.getSpecificFrame(animation, frame);
        updateTextureAndFlushIfNeeded(textureRegion);

        if (Math.abs(rotation) < ROTATION_EPSILON) {
            queueFrameUnrotated(textureRegion,
                                x - originX, y - originY,
                                x - originX + width, y - originY + height,
                                1.0, 1.0, 1.0);
        } else {
            queueFrame(textureRegion, x, y, originX, originY, width, height, rotation);
        }
        this.nFrames += 1;
    }

    @Override
    public void draw(
            final TextureRegion textureRegion,
            final double x0,
            final double y0,
            final double x1,
            final double y1,
            final double r,
            final double g,
            final double b
    ) {
        updateTextureAndFlushIfNeeded(textureRegion);
        queueFrameUnrotated(textureRegion, x0, y0, x1, y1, r, g, b);
        this.nFrames += 1;
    }

    protected void updateTextureAndFlushIfNeeded(final TextureRegion textureRegion) {
        if (this.activeTexture == null) {
            this.activeTexture = textureRegion.getTexture();
        }

        final var needToChangeTexture = !textureRegion.getTexture().equals(this.activeTexture);
        final var batchIsFull = this.nFrames >= this.maxFramesPerBatch - 1;
        if (needToChangeTexture || batchIsFull) {
            flush(this.activeTexture, this.activeTransformation);
            this.nFrames = 0;
            this.activeTexture = textureRegion.getTexture();
        }
    }
}
