package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SplitOnDeath;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class SplitOnDeathSystemTest {
    @Test
    void largeSlimeSpawnsMultipleSlimesOnDeath() {
        SplitOnDeathSystem system = new SplitOnDeathSystem();
        World world = fi.jakojaannos.roguelite.engine.ecs.newecs.World.createNew();
        EntityManager entityManager = world.getEntityManager();

        final var time = new Time(new SimpleTimeManager(20));
        world.provideResource(Time.class, time);

        Entity slime = SlimeArchetype.createLargeSlime(entityManager, 0.0, 0.0);
        entityManager.addComponentTo(slime, new DeadTag());

        entityManager.applyModifications();

        long amountBefore = entityManager.getEntitiesWith(SplitOnDeath.class).count();
        system.tick(Stream.of(slime), world);
        entityManager.applyModifications();
        long amountAfter = entityManager.getEntitiesWith(SplitOnDeath.class).count();

        assertTrue(amountAfter > amountBefore);
    }
}
