package fi.jakojaannos.roguelite.engine.view.sprite;

import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public final class Sprite<TTexture extends Texture> {
    @Getter private final List<TextureRegion<TTexture>> frames;
    @Getter private final Map<String, Animation> animations;

    public int getAnimationFrameCount(final String animation) {
        return this.animations.get(animation).frameCount();
    }

    public TextureRegion<TTexture> getSpecificFrame(final String animation, final int frame) {
        val actualAnimation = this.animations.getOrDefault(animation, this.animations.get("default"));
        return this.frames.get(actualAnimation.getFrameIndexOfFrame(frame));
    }

    public TextureRegion<TTexture> getFrame(final String animation, final double time) {
        return this.frames.get(this.animations.get(animation)
                                              .getFrameIndexAtTime(time));
    }

    public static <TTexture extends Texture> Sprite<TTexture> ofSingleFrame(final TextureRegion<TTexture> region) {
        return new Sprite<>(List.of(region),
                            Map.of("default", Animation.forSingleFrame(0, Double.POSITIVE_INFINITY)));
    }
}
