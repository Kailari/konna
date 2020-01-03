package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SlimeAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SlimeSharedAI;
import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlimeDeathHandlerSystemTest {

    @Test
    void largeSlimeSpawnsMultipleSlimesOnDeath() {
        SlimeDeathHandlerSystem system = new SlimeDeathHandlerSystem();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);

        Entity slime = SlimeArchetype.createLargeSlime(entityManager, 0.0, 0.0);
        entityManager.addComponentTo(slime, new DeadTag());

        entityManager.applyModifications();

        long amountBefore = entityManager.getEntitiesWith(SlimeAI.class).count();
        system.tick(Stream.of(slime), world);
        entityManager.applyModifications();
        long amountAfter = entityManager.getEntitiesWith(SlimeAI.class).count();

        assertTrue(amountAfter > amountBefore);
    }


    @Test
    void sharedAIComponentIsRemovedOnDeath() {
        SlimeDeathHandlerSystem system = new SlimeDeathHandlerSystem();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);

        Vector2d vec = new Vector2d(0.0, 0.0);
        Entity slime1 = SlimeArchetype.createSmallSlimeWithInitialVelocity(entityManager, 0.0, 0.0, vec, 0.0);
        Entity slime2 = SlimeArchetype.createSmallSlimeWithInitialVelocity(entityManager, 0.0, 0.0, vec, 0.0);
        Entity slime3 = SlimeArchetype.createSmallSlimeWithInitialVelocity(entityManager, 0.0, 0.0, vec, 0.0);
        Entity slime4 = SlimeArchetype.createSmallSlimeWithInitialVelocity(entityManager, 0.0, 0.0, vec, 0.0);

        SlimeSharedAI sharedAI = new SlimeSharedAI();
        entityManager.addComponentTo(slime1, sharedAI);
        entityManager.addComponentTo(slime2, sharedAI);
        entityManager.addComponentTo(slime3, sharedAI);
        entityManager.addComponentTo(slime4, sharedAI);

        sharedAI.slimes.add(slime1);
        sharedAI.slimes.add(slime2);
        sharedAI.slimes.add(slime3);
        sharedAI.slimes.add(slime4);

        entityManager.addComponentTo(slime4, new DeadTag());
        system.tick(Stream.of(slime4), world);

        assertTrue(sharedAI.slimes.size() == 3
                && !sharedAI.slimes.contains(slime4));
    }

}
