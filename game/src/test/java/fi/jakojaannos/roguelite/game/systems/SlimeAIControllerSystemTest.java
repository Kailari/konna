package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.archetypes.PlayerArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.SlimeAI;
import fi.jakojaannos.roguelite.game.data.components.SlimeSharedAI;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlimeAIControllerSystemTest {

    @Test
    void smallerSlimesCanRegroupIntoLargerSlime() {
        SlimeAIControllerSystem system = new SlimeAIControllerSystem();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);

        world.getResource(Players.class).player =
                PlayerArchetype.create(entityManager, new Transform(100, 100));


        Vector2d vec = new Vector2d(0.0, 0.0);
        SlimeSharedAI sharedAI = new SlimeSharedAI();
        List<Entity> list = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Entity slime = SlimeArchetype.createSmallSlimeWithInitialVelocity(entityManager, 0.0, 0.0, vec, 0.0);
            entityManager.addComponentTo(slime, sharedAI);
            sharedAI.slimes.add(slime);
            list.add(slime);
        }

        sharedAI.regroupPos.set(0.0, 0.0);
        sharedAI.regrouping = true;

        system.tick(list.stream(), world, 0.2);
        entityManager.applyModifications();

        List<EntityManager.EntityComponentPair<SlimeAI>> entities =
                entityManager
                        .getEntitiesWith(SlimeAI.class)
                        .collect(Collectors.toList());


        assertEquals(1, entities.size());

        boolean hasOneLargeSlime =
                (entities.size() == 1
                        && entities.get(0).getComponent().slimeSize == 2);

        assertTrue(hasOneLargeSlime);

    }

}
