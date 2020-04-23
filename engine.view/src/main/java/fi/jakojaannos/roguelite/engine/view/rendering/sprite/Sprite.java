package fi.jakojaannos.roguelite.engine.view.rendering.sprite;

import java.util.List;
import java.util.Map;

import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;

public final class Sprite {
    private final int rows;
    private final int columns;
    private final List<TextureRegion> frames;
    private final Map<String, Animation> animations;

    public float getRows() {
        return this.rows;
    }

    public float getColumns() {
        return this.columns;
    }

    public Sprite(
            final int rows,
            final int columns,
            final List<TextureRegion> frames,
            final Map<String, Animation> animations
    ) {
        this.rows = rows;
        this.columns = columns;
        this.frames = frames;
        this.animations = animations;
    }

    public static Sprite ofSingleFrame(final TextureRegion region) {
        return new Sprite(1,
                          1,
                          List.of(region),
                          Map.of("default", Animation.forSingleFrame(0, Double.POSITIVE_INFINITY)));
    }

    public int getAnimationFrameCount(final String animation) {
        return this.animations.get(animation).frameCount();
    }

    public TextureRegion getSpecificFrame(final String animation, final int frame) {
        final var actualAnimation = this.animations.getOrDefault(animation, this.animations.get("default"));
        return this.frames.get(actualAnimation.getFrameIndexOfFrame(frame));
    }

    public TextureRegion getFrame(final String animation, final double time) {
        return this.frames.get(this.animations.get(animation)
                                              .getFrameIndexAtTime(time));
    }
}
