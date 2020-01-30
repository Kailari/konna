package fi.jakojaannos.roguelite.game.data.components;

import lombok.AllArgsConstructor;

import fi.jakojaannos.roguelite.engine.ecs.Component;

@AllArgsConstructor
public class InAir implements Component {
    public long flightStartTimeStamp;
    public long flightDuration;
}
