package fi.jakojaannos.roguelite.engine.view.rendering.sprite;

import java.util.List;
import java.util.Map;

import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;

public final class Sprite {
    private final List<TextureRegion> frames;
    private final Map<String, Animation> animations;

    public Sprite(final List<TextureRegion> frames, final Map<String, Animation> animations) {
        this.frames = frames;
        this.animations = animations;
    }

    public static Sprite ofSingleFrame(final TextureRegion region) {
        return new Sprite(List.of(region),
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
