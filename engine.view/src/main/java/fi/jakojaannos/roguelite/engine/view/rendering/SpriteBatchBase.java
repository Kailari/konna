package fi.jakojaannos.roguelite.engine.view.rendering;

import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

@Slf4j
public abstract class SpriteBatchBase<TTexture extends Texture, TCamera extends Camera>
        implements SpriteBatch<TTexture, TCamera> {
    private final int maxFramesPerBatch;

    private boolean beginCalled;
    private Matrix4f activeTransformation;
    private TTexture activeTexture;
    @Getter(AccessLevel.PROTECTED) private int nFrames;

    protected SpriteBatchBase(int maxFramesPerBatch) {
        this.maxFramesPerBatch = maxFramesPerBatch;
    }

    /**
     * Flushes the currently queued sprites.
     *
     * @param texture        texture to use
     * @param transformation global transformations to apply
     */
    protected abstract void flush(
            TTexture texture,
            @Nullable Matrix4f transformation
    );

    /**
     * Queues a new sprite animation frame for rendering. Passing in -1 as the frame renders the
     * whole texture.
     *
     * @param texture texture to render
     * @param x       world x-coordinate to place the sprite to
     * @param y       world y-coordinate to place the sprite to
     * @param width   horizontal size of the sprite in world units
     * @param height  vertical size of the sprite in world units
     */
    protected abstract void queueFrame(
            TextureRegion<TTexture> texture,
            double x,
            double y,
            double originX,
            double originY,
            double width,
            double height,
            double rotation
    );

    @Override
    public void begin(@Nullable Matrix4f transformation) {
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
            Sprite<TTexture> sprite,
            String animation,
            int frame,
            double x,
            double y,
            double originX,
            double originY,
            double width,
            double height,
            double rotation
    ) {
        val textureRegion = sprite.getSpecificFrame(animation, frame);
        if (this.activeTexture == null) {
            this.activeTexture = textureRegion.getTexture();
        }

        val needToChangeTexture = !textureRegion.getTexture().equals(this.activeTexture);
        val batchIsFull = this.nFrames >= this.maxFramesPerBatch - 1;
        if (needToChangeTexture || batchIsFull) {
            flush(this.activeTexture, this.activeTransformation);
            this.nFrames = 0;
            this.activeTexture = textureRegion.getTexture();
        }

        queueFrame(textureRegion, x, y, originX, originY, width, height, rotation);
        this.nFrames += 1;
    }
}
