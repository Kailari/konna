package fi.jakojaannos.roguelite.engine.view.rendering.sprite;

import org.joml.Matrix4f;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;

public interface SpriteBatch
        extends AutoCloseable {
    /**
     * Begins a new rendering batch. A batch must be finished using {@link #end()}, before calling {@link #begin}
     * again.
     */
    default void begin() {
        begin(null);
    }

    /**
     * Begins a new rendering batch. Applies provided global transformation. A batch must be finished using {@link
     * #end()}, before calling {@link #begin} again.
     *
     * @param transformation additional global transformation to apply. May be <code>null</code>
     */
    void begin(@Nullable Matrix4f transformation);

    /**
     * Renders the sprite at given coordinates with given size. Uses the default animation and first available frame.
     *
     * @param sprite sprite to render
     * @param x      world x-coordinate where the sprite should be placed
     * @param y      world y-coordinate where the sprite should be placed
     * @param width  horizontal size of the sprite in world units
     * @param height vertical size of the sprite in world units
     */
    default void draw(Sprite sprite, double x, double y, double width, double height) {
        draw(sprite, "default", 0, x, y, width, height);
    }

    /**
     * Renders the sprite at given coordinates with given size. Uses the given animation and the frame.
     *
     * @param sprite    sprite to render
     * @param animation name of the animation to use
     * @param frame     frame to render
     * @param x         world x-coordinate where the sprite should be placed
     * @param y         world y-coordinate where the sprite should be placed
     * @param width     horizontal size of the sprite in world units
     * @param height    vertical size of the sprite in world units
     */
    default void draw(
            Sprite sprite,
            String animation,
            int frame,
            double x,
            double y,
            double width,
            double height
    ) {
        draw(sprite, animation, frame, x, y, 0.0, 0.0, width, height, 0.0);
    }

    /**
     * Renders the sprite at given coordinates with given size.  Uses the given animation and the frame. Additionally
     * uses the given origin and rotation to offset the sprite.
     *
     * @param sprite    sprite to render
     * @param animation name of the animation to use
     * @param frame     frame to render
     * @param x         world x-coordinate where the sprite should be placed
     * @param y         world y-coordinate where the sprite should be placed
     * @param originX   origin offset on x-axis
     * @param originY   origin offset on y-axis
     * @param width     horizontal size of the sprite in world units
     * @param height    vertical size of the sprite in world units
     * @param rotation  rotation, in radians, counter-clockwise
     */
    void draw(
            Sprite sprite,
            String animation,
            int frame,
            double x,
            double y,
            double originX,
            double originY,
            double width,
            double height,
            double rotation
    );

    void draw(
            TextureRegion region,
            double x0,
            double y0,
            double x1,
            double y1,
            double r,
            double g,
            double b
    );

    void end();
}
