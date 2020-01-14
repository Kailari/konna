package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InAir implements Component {
    public long flightStartTimeStamp, flightDuration;
}
