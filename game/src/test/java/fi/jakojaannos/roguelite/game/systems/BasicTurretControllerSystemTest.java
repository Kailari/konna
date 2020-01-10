package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.game.data.components.BasicTurretComponent;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BasicTurretControllerSystemTest {

    private EntityManager entityManager;
    private World world;
    private BasicTurretControllerSystem system;
    private Entity turret;
    private BasicTurretComponent turretComp;

    @BeforeEach
    void beforeEach() {
        entityManager = EntityManager.createNew(256, 32);
        world = World.createNew(entityManager);
        world.createOrReplaceResource(Time.class, new Time(new SimpleTimeManager(20)));

        system = new BasicTurretControllerSystem();
        turret = entityManager.createEntity();
        entityManager.addComponentTo(turret, new Transform(0.0, 0.0));
        turretComp = new BasicTurretComponent();
        turretComp.targetingRadiusSquared = 7.0 * 7.0;
        entityManager.addComponentTo(turret, turretComp);
    }

    @Test
    void nearbyValidEntityIsTargeted() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new EnemyTag());
        entityManager.addComponentTo(target, new Transform(4.0, 4.0));

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertEquals(target, turretComp.target);
    }

    @Test
    void entitiesTooFarAreNotTargeted() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new EnemyTag());
        entityManager.addComponentTo(target, new Transform(420.0, 666.0));

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertNull(turretComp.target);
    }

    @Test
    void entitiesWithoutEnemyTagAreNotTargeted() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new Transform(0.0, 0.0));

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertNull(turretComp.target);
    }

    @Test
    void targetIsRemovedAfterBecomingInvalid() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new EnemyTag());
        entityManager.addComponentTo(target, new Transform(4.0, 4.0));
        turretComp.target = target;

        entityManager.removeComponentFrom(target, EnemyTag.class);

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertNull(turretComp.target);
    }

    @Test
    void newTargetIsNotSetWhenPreviousOneIsValid() {
        Entity originalTarget = entityManager.createEntity();
        entityManager.addComponentTo(originalTarget, new EnemyTag());
        entityManager.addComponentTo(originalTarget, new Transform(4.0, 4.0));
        turretComp.target = originalTarget;

        for (int i = 0; i < 10; i++) {
            Entity newTarget = entityManager.createEntity();
            entityManager.addComponentTo(newTarget, new EnemyTag());
            entityManager.addComponentTo(newTarget, new Transform(1.0, 1.0));
        }

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertEquals(originalTarget, turretComp.target);
    }


    @Test
    void turretShootsWhenTargetIsSet() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new EnemyTag());
        entityManager.addComponentTo(target, new Transform(4.0, 4.0));
        turretComp.target = target;

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);
        entityManager.applyModifications();

        // after weapon rework this can be changed to something like this
        // assertTrue(weaponAttackSystem.attack);

        long projectiles = entityManager.getEntitiesWith(ProjectileStats.class).count();
        assertEquals(1, projectiles);
    }

    @Test
    void turretDoesNotShootsWhenTargetIsNotSet() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new EnemyTag());
        entityManager.addComponentTo(target, new Transform(400.0, 400.0));

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        // after weapon rework this can be changed to something like this
        // assertFalse(weaponAttackSystem.attack);

        long projectiles = entityManager.getEntitiesWith(ProjectileStats.class).count();
        assertEquals(0, projectiles);
    }

    @Test
    void targetGoingOutOfRangeIsRemovedAsTarget() {
        Entity target = entityManager.createEntity();
        entityManager.addComponentTo(target, new EnemyTag());
        entityManager.addComponentTo(target, new Transform(666.6, 666.6));
        turretComp.target = target;

        entityManager.applyModifications();
        system.tick(Stream.of(turret), world);

        assertNull(turretComp.target);
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

        assertTrue(insideRange.contains(turretComp.target));

    }


}
