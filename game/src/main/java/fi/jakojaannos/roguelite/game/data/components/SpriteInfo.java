package fi.jakojaannos.roguelite.game.data.components;

import java.util.List;

public class SpriteInfo {
    public String spriteName;
    public String animationName = "default";
    public int zLayer;

    public List<Integer> frames = List.of(0);
    public int frameIndex = 0;

    public double frameTimer;

    public SpriteInfo(final String spriteName) {
        this.spriteName = spriteName;
    }

    public int getCurrentFrame() {
        return this.frames.get(this.frameIndex);
    }
}
