package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;

public class SplitOnDeath implements Component {
    /** "Size" of the entity */
    public double size;

    /** Percentage of the size lost on each split */
    public double sizeLossPercentage;

    /** Number of offspring spawned when split */
    public int offspringAmount = 4;

    /** Minimum force applied to spawned offspring */
    public double minSpawnForce = 1.0;
    /** Maximum force applied to spawned offspring */
    public double maxSpawnForce = 6.0;

    /** Minimum time the spawned offspring will spend in air after spawning */
    public int minSpawnFlightDurationInTicks = 7;
    /** Maximum time the spawned offspring will spend in air after spawning */
    public int maxSpawnFlightDurationInTicks = 14;

    public SplitOnDeath(final double size) {
        this.size = size;
    }
}
