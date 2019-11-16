package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class SpriteInfo implements Component {
    public String spriteName;
    public int zLayer;

    public List<Integer> frames = List.of(-1);
    public int frameIndex = 0;

    public double frameTimer;

    public int getCurrentFrame() {
        return this.frames.get(this.frameIndex);
    }
}
