package fi.jakojaannos.roguelite.game.data.resources;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.game.data.DamageSource;

public record RecentExplosion(
        Vector2d location,
        double damage,
        double radiusSquared,
        double pushForce,
        DamageSource<?>damageSource
) {
}
