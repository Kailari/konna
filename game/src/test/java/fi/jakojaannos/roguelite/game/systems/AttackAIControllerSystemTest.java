package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.legacy.World;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.systems.characters.ai.AttackAIControllerSystem;

import static org.junit.jupiter.api.Assertions.*;

public class AttackAIControllerSystemTest {
    private EntityManager entityManager;
    private World world;
    private AttackAIControllerSystem system;
    private Entity turret;
    private AttackAI attackAi;

    @BeforeEach
    void beforeEach() {
        world = fi.jakojaannos.roguelite.engine.ecs.World.createNew();
        entityManager = world.getEntityManager();
        world.provideResource(Time.class, new Time(new SimpleTimeManager(20)));

        system = new AttackAIControllerSystem();
        turret = entityManager.createEntity();
        entityManager.addComponentTo(turret, new Transform(0.0, 0.0));
        entityManager.addComponentTo(turret, new AttackAbility(DamageSource.Generic.UNDEFINED));
        entityManager.addComponentTo(turret, attackAi = new AttackAI(EnemyTag.class, 7.0));
        entityManager.addComponentTo(turret, new WeaponInput());
    }

    @Test
    void nearbyValidEntityIsTargeted() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new EnemyTag());
        entityManager.addComponentTo(target, new Transform(4.0, 4.0));

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertEquals(target, attackAi.getAttackTarget().orElseThrow());
    }

    @Test
    void entitiesTooFarAreNotTargeted() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new EnemyTag());
        entityManager.addComponentTo(target, new Transform(420.0, 666.0));

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertTrue(attackAi.getAttackTarget().isEmpty());
    }

    @Test
    void entitiesWithoutEnemyTagAreNotTargeted() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new Transform(0.0, 0.0));

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertTrue(attackAi.getAttackTarget().isEmpty());
    }

    @Test
    void targetIsRemovedAfterBecomingInvalid() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new EnemyTag());
        entityManager.addComponentTo(target, new Transform(4.0, 4.0));
        attackAi.setAttackTarget(target);

        entityManager.removeComponentFrom(target, EnemyTag.class);

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertTrue(attackAi.getAttackTarget().isEmpty());
    }

    @Test
    void newTargetIsNotSetWhenPreviousOneIsValid() {
        Entity originalTarget = entityManager.createEntity();
        entityManager.addComponentTo(originalTarget, new EnemyTag());
        entityManager.addComponentTo(originalTarget, new Transform(4.0, 4.0));
        attackAi.setAttackTarget(originalTarget);

        for (int i = 0; i < 10; i++) {
            Entity newTarget = entityManager.createEntity();
            entityManager.addComponentTo(newTarget, new EnemyTag());
            entityManager.addComponentTo(newTarget, new Transform(1.0, 1.0));
        }

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertEquals(originalTarget, attackAi.getAttackTarget().orElseThrow());
    }

    @Test
    void attackTargetIsClearedIfTargetGoesOutOfRange() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new EnemyTag());
        entityManager.addComponentTo(target, new Transform(666.6, 666.6));
        attackAi.setAttackTarget(target);

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertTrue(attackAi.getAttackTarget().isEmpty());
    }

    @Test
    void inCaseThereAreManyTargetsNearbyOneIsChosen() {
        List<Entity> insideRange = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Entity target = entityManager.createEntity();
            entityManager.addComponentTo(target, new EnemyTag());
            entityManager.addComponentTo(target, new Transform(1.0, 1.0));
            insideRange.add(target);
        }

        for (int i = 0; i < 5; i++) {
            Entity target = entityManager.createEntity();
            entityManager.addComponentTo(target, new EnemyTag());
            entityManager.addComponentTo(target, new Transform(333.0, 333.0));
        }

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertTrue(insideRange.contains(attackAi.getAttackTarget().orElseThrow()));
    }
}
