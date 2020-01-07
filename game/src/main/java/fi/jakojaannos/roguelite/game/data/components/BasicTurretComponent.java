package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;

import javax.annotation.Nullable;


public class BasicTurretComponent implements Component {

    public double targetingRadiusSquared = 25.0 * 25.0;
    public double projectileSpeed = 10.0;

    public long lastShotTimestamp = -1000L, shootingCoolDownInTicks = 75L;

    @Nullable
    public Entity target = null;


}
