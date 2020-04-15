package fi.jakojaannos.roguelite.game.weapons;

import org.joml.Vector2d;

public class ProjectileFiringAttributes {
    public Vector2d weaponOffset = new Vector2d(0.0, 0.0);
    public long timeBetweenShots = 20;
    public double projectileSpeed = 40.0;
    public double spread;
    public double projectileSpeedNoise;
    public long projectileLifetimeInTicks = -1;
    public double projectilePushForce;
    public double damage = 1.0;
}
