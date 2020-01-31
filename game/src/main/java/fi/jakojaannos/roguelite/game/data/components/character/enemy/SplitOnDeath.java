package fi.jakojaannos.roguelite.game.data.components.character.enemy;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class SplitOnDeath implements Component {
    /** "Size" of the entity */
    public double size = 3;

    /** Percentage of the size lost on each split */
    public double sizeLossPercentage = 0.0;

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
}
