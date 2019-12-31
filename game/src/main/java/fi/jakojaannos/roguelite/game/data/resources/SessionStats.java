package fi.jakojaannos.roguelite.game.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Resource;

public class SessionStats implements Resource {
    public boolean shouldRestart = false;
    public long beginTimeStamp, endTimeStamp;
}
