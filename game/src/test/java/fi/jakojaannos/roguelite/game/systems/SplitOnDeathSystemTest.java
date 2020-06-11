package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SplitOnDeath;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SplitOnDeathSystemTest {
    @Test
    void largeSlimeSpawnsMultipleSlimesOnDeath() {
        SplitOnDeathSystem system = new SplitOnDeathSystem();
        World world = World.createNew();
        EntityManager entityManager = world.getEntityManager();

        world.registerResource(TimeManager.class, new SimpleTimeManager(20));

        Entity slime = SlimeArchetype.createLargeSlime(entityManager, new Transform(0.0, 0.0))
                                     .asLegacyEntity();
        entityManager.addComponentTo(slime, new DeadTag());

        entityManager.applyModifications();

        long amountBefore = entityManager.getEntitiesWith(SplitOnDeath.class).count();
        system.tick(Stream.of(slime), world);
        entityManager.applyModifications();
        long amountAfter = entityManager.getEntitiesWith(SplitOnDeath.class).count();

        assertTrue(amountAfter > amountBefore);
    }
}
