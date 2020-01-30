package fi.jakojaannos.roguelite.game.data.components;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

import fi.jakojaannos.roguelite.engine.ecs.Component;

@NoArgsConstructor
@AllArgsConstructor
public class SpriteInfo implements Component {
    public String spriteName;
    public String animationName = "default";
    public int zLayer;

    public List<Integer> frames = List.of(0);
    public int frameIndex = 0;

    public double frameTimer;

    public int getCurrentFrame() {
        return this.frames.get(this.frameIndex);
    }
}
