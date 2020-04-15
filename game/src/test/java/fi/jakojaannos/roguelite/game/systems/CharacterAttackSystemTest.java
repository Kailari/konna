package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;
import fi.jakojaannos.roguelite.game.systems.characters.CharacterAttackSystem;
import fi.jakojaannos.roguelite.game.weapons.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterAttackSystemTest {
    private CharacterAttackSystem system;
    private World world;
    private Entity entity;
    private WeaponInput weaponInput;
    private AttackAbility attackAbility;
    private ProjectileFiringModule.Attributes projectileStats;
    private SimpleTimeManager time;

    @BeforeEach
    void beforeEach() {
        this.system = new CharacterAttackSystem();
        this.world = World.createNew();
        EntityManager entityManager = world.getEntityManager();

        world.registerResource(TimeManager.class, time = new SimpleTimeManager(20));

        entity = entityManager.createEntity();
        this.weaponInput = new WeaponInput();
        this.weaponInput.attack = false;
        this.attackAbility = new AttackAbility(
                new DamageSource.LegacyEntity(entity),
                CollisionLayer.PLAYER,
                0.0,
                0.0);
        //this.weaponStats = WeaponStats.builder().build();
        entityManager.addComponentTo(entity, new Transform(0.0, 0.0));
        entityManager.addComponentTo(entity, new Velocity());
        entityManager.addComponentTo(entity, this.weaponInput);
        entityManager.addComponentTo(entity, this.attackAbility);

        final var wepInv = new WeaponInventory(3);
        final var weaponAttributes = new WeaponAttributes();
        projectileStats = new ProjectileFiringModule.Attributes(new Vector2d(0.0),
                                                                20,
                                                                10,
                                                                2.0,
                                                                0.0,
                                                                15,
                                                                0.0,
                                                                1.0);
        weaponAttributes.put(ProjectileFiringModule.class, projectileStats);
        wepInv.equip(0, new InventoryWeapon(Weapons.PLAYER_AR));
        entityManager.addComponentTo(entity, wepInv);

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

        weaponInput.attack = false;
        this.system.tick(Stream.of(entity), this.world);
        this.world.getEntityManager().applyModifications();

        assertEquals(0, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void characterShootsWhenInputIsTrue() {
        attackAbility.targetPosition.set(10.0, 10.0);

        weaponInput.attack = true;
        this.system.tick(Stream.of(entity), this.world);
        this.world.getEntityManager().applyModifications();

        assertEquals(1, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void attackRateLimitsWhenCharacterCanShoot() {
        attackAbility.targetPosition.set(10.0, 10.0);

        weaponInput.attack = true;
        for (int i = 0; i < 65; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.world.getEntityManager().applyModifications();
            time.refresh();
        }

        assertEquals(6, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void characterStopsAttackingWhenInputIsSetToFalse() {
        attackAbility.targetPosition.set(10.0, 10.0);

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

    @Test
    void switchingWeaponEquipsNewWeapon() {
        // switch weapon slot

        // tick system

        // assert that new weapon had its .equip(...) called

    }

    @Test
    void switchingWeaponUnequipsPreviousWeapon() {
        // switch

        // tick system

        // assert that old weapon has its .unequip(...) called

    }
}
