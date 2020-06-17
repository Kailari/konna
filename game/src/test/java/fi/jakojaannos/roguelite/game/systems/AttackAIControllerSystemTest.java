package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.characters.ai.AttackAIControllerSystem;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttackAIControllerSystemTest {
    private AttackAI attackAi;
    private EntityHandle target;

    void initialState(final World world) {
        world.registerResource(new Players());

        world.createEntity(new Transform(),
                           new AttackAbility(DamageSource.Generic.UNDEFINED,
                                             CollisionLayer.PLAYER_PROJECTILE,
                                             0,
                                             0,
                                             0),
                           new WeaponInput(),
                           attackAi = new AttackAI(EnemyTag.class, 7.0));
    }

    @Test
    void nearbyValidEntityIsTargeted() {
        whenGame().withSystems(new AttackAIControllerSystem())
                  .withState(this::initialState)
                  .withState(world -> target = world.createEntity(new EnemyTag(), new Transform(4.0, 4.0)))
                  .runsSingleTick()
                  .expect(state -> assertEquals(target, attackAi.getAttackTarget().orElseThrow()));
    }

    @Test
    void entitiesTooFarAreNotTargeted() {
        whenGame().withSystems(new AttackAIControllerSystem())
                  .withState(this::initialState)
                  .withState(world -> target = world.createEntity(new EnemyTag(), new Transform(420.0, 666.0)))
                  .runsSingleTick()
                  .expect(state -> assertTrue(attackAi.getAttackTarget().isEmpty()));
    }

    @Test
    void entitiesWithoutEnemyTagAreNotTargeted() {
        whenGame().withSystems(new AttackAIControllerSystem())
                  .withState(this::initialState)
                  .withState(world -> target = world.createEntity(new Transform(420.0, 666.0)))
                  .runsSingleTick()
                  .expect(state -> assertTrue(attackAi.getAttackTarget().isEmpty()));
    }

    @Test
    void targetIsRemovedAfterBecomingInvalid() {
        whenGame().withSystems(new AttackAIControllerSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      target = world.createEntity(new EnemyTag(), new Transform(420.0, 666.0));
                      attackAi.setAttackTarget(target);

                      target.removeComponent(EnemyTag.class);
                  })
                  .runsSingleTick()
                  .expect(state -> assertTrue(attackAi.getAttackTarget().isEmpty()));
    }

    @Test
    void newTargetIsNotSetWhenPreviousOneIsValid() {
        whenGame().withSystems(new AttackAIControllerSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      target = world.createEntity(new EnemyTag(), new Transform(4.0, 4.0));
                      attackAi.setAttackTarget(target);

                      for (int i = 0; i < 10; i++) {
                          world.createEntity(new EnemyTag(), new Transform(i / 10.0, 1.0));
                      }
                  })
                  .runsSingleTick()
                  .expect(state -> assertEquals(target, attackAi.getAttackTarget().orElseThrow()));
    }

    @Test
    void attackTargetIsClearedIfTargetGoesOutOfRange() {
        whenGame().withSystems(new AttackAIControllerSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      target = world.createEntity(new Transform(420.0, 666.0));
                      attackAi.setAttackTarget(target);
                  })
                  .runsSingleTick()
                  .expect(state -> assertTrue(attackAi.getAttackTarget().isEmpty()));
    }

    @Test
    void inCaseThereAreManyTargetsNearbyOneIsChosen() {
        final var insideRange = new ArrayList<EntityHandle>();
        whenGame().withSystems(new AttackAIControllerSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      for (int i = 0; i < 5; i++) {
                          final var entity = world.createEntity(new EnemyTag(), new Transform(1.0, 1.0));
                          insideRange.add(entity);
                      }

                      for (int i = 0; i < 5; i++) {
                          world.createEntity(new EnemyTag(), new Transform(333.0, 333.0));
                      }
                  })
                  .runsSingleTick()
                  .expect(state -> assertTrue(insideRange.contains(attackAi.getAttackTarget().orElseThrow())));
    }
}
