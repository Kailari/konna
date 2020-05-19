package fi.jakojaannos.roguelite.game.data.resources;

import org.joml.Vector2d;

public record RecentExplosion(
        Vector2d location,
        double damage,
        double radiusSquared
) {
}
