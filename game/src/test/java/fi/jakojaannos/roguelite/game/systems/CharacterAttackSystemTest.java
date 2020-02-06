package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;
import fi.jakojaannos.roguelite.game.systems.characters.CharacterAttackSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterAttackSystemTest {
    private CharacterAttackSystem system;
    private World world;
    private Entity entity;
    private WeaponInput weaponInput;
    private AttackAbility attackAbility;
    private WeaponStats weaponStats;
    private SimpleTimeManager time;

    @BeforeEach
    void beforeEach() {
        this.system = new CharacterAttackSystem();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        this.world = World.createNew(entityManager);

        world.provideResource(Time.class, new Time(time = new SimpleTimeManager(20L)));

        entity = entityManager.createEntity();
        this.weaponInput = new WeaponInput();
        this.weaponInput.attack = false;
        this.attackAbility = new AttackAbility(new DamageSource.Entity(entity));
        this.weaponStats = new WeaponStats();
        entityManager.addComponentTo(entity, new Transform(0.0, 0.0));
        entityManager.addComponentTo(entity, new Velocity());
        entityManager.addComponentTo(entity, this.weaponInput);
        entityManager.addComponentTo(entity, this.attackAbility);
        entityManager.addComponentTo(entity, this.weaponStats);

        entityManager.applyModifications();

        // Warm-up
        for (int i = 0; i < 100; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.world.getEntityManager().applyModifications();
        }
    }

    @Test
    void characterDoesNotShootWhenInputIsFalse() {
        attackAbility.targetPosition.set(10.0, 10.0);
        weaponStats.timeBetweenShots = 20;

        weaponInput.attack = false;
        this.system.tick(Stream.of(entity), this.world);
        this.world.getEntityManager().applyModifications();

        assertEquals(0, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void characterShootsWhenInputIsTrue() {
        attackAbility.targetPosition.set(10.0, 10.0);
        weaponStats.timeBetweenShots = 20;

        weaponInput.attack = true;
        this.system.tick(Stream.of(entity), this.world);
        this.world.getEntityManager().applyModifications();

        assertEquals(1, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void attackRateLimitsWhenCharacterCanShoot() {
        attackAbility.targetPosition.set(10.0, 10.0);
        weaponStats.timeBetweenShots = 20;

        weaponInput.attack = true;
        for (int i = 0; i < 65; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.world.getEntityManager().applyModifications();
            time.refresh();
        }

        assertEquals(4, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void characterStopsAttackingWhenInputIsSetToFalse() {
        attackAbility.targetPosition.set(10.0, 10.0);
        weaponStats.timeBetweenShots = 20;

        weaponInput.attack = true;
        this.system.tick(Stream.of(entity), this.world);
        this.world.getEntityManager().applyModifications();

        weaponInput.attack = false;
        for (int i = 0; i < 200; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.world.getEntityManager().applyModifications();
            time.refresh();
        }

        assertEquals(1, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }
}
