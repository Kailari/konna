package fi.jakojaannos.roguelite.game.systems;

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

import static org.junit.jupiter.api.Assertions.*;

class CharacterAttackSystemTest {
    private CharacterAttackSystem system;
    private World world;
    private Entity entity;
    private WeaponInput weaponInput;
    private AttackAbility attackAbility;
    private SimpleTimeManager time;
    private TestState previousWeapon;
    private TestState newWeapon;

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
        entityManager.addComponentTo(entity, new Transform(0.0, 0.0));
        entityManager.addComponentTo(entity, new Velocity());
        entityManager.addComponentTo(entity, this.weaponInput);
        entityManager.addComponentTo(entity, this.attackAbility);

        final var wepInv = new WeaponInventory(3);
        wepInv.equip(0, new InventoryWeapon(Weapons.PLAYER_AR));
        wepInv.equip(1, new InventoryWeapon(createNotifyingWeapon(previousWeapon = new TestState())));
        wepInv.equip(2, new InventoryWeapon(createNotifyingWeapon(newWeapon = new TestState())));

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
    void previouslyEquippedSlotIsUpdated() {
        attackAbility.previousEquippedSlot = 0;
        attackAbility.equippedSlot = 2;

        this.system.tick(Stream.of(entity), this.world);

        assertEquals(2, attackAbility.previousEquippedSlot);

        this.system.tick(Stream.of(entity), this.world);
        assertEquals(2, attackAbility.previousEquippedSlot);

        attackAbility.equippedSlot = 1;
        this.system.tick(Stream.of(entity), this.world);
        assertEquals(1, attackAbility.previousEquippedSlot);
    }

    @Test
    void switchingWeaponEquipsNewWeapon() {
        // switch weapon slot
        // NOTE: normally attackAbility.previousEquippedSlot shouldn't be modified
        attackAbility.previousEquippedSlot = 1;
        attackAbility.equippedSlot = 2;

        // tick system
        this.system.tick(Stream.of(entity), this.world);

        // assert that new weapon had its .equip(...) called
        assertTrue(newWeapon.equipCalled);
    }

    @Test
    void switchingWeaponDoesntEquipOldWeapon() {
        attackAbility.previousEquippedSlot = 1;
        attackAbility.equippedSlot = 2;

        this.system.tick(Stream.of(entity), this.world);

        assertFalse(previousWeapon.equipCalled);
    }

    @Test
    void switchingWeaponUnequipsPreviousWeapon() {
        attackAbility.previousEquippedSlot = 1;
        attackAbility.equippedSlot = 2;

        this.system.tick(Stream.of(entity), this.world);

        assertTrue(previousWeapon.unequipCalled);
    }

    @Test
    void switchingWeaponDoesntUnequipNewWeapon() {
        attackAbility.previousEquippedSlot = 1;
        attackAbility.equippedSlot = 2;

        this.system.tick(Stream.of(entity), this.world);

        assertFalse(newWeapon.unequipCalled);
    }

    private static ModularWeapon createNotifyingWeapon(final TestState state) {
        final var notifyOnEquipModule = new WeaponModule<TestState, NoAttributes>() {
            @Override
            public TestState getDefaultState() {
                return state;
            }

            @Override
            public void register(final WeaponHooks hooks) {
                hooks.registerWeaponEquip(this, this::equip, Phase.TRIGGER);
                hooks.registerWeaponUnequip(this, this::unequip, Phase.TRIGGER);
            }

            public void equip(
                    final TestState state,
                    final NoAttributes attributes,
                    final WeaponEquipEvent event,
                    final ActionInfo info
            ) {
                state.equipCalled = true;
            }

            public void unequip(
                    final TestState state,
                    final NoAttributes attributes,
                    final WeaponUnequipEvent event,
                    final ActionInfo info
            ) {
                state.unequipCalled = true;
            }
        };

        return new ModularWeapon(new ModularWeapon.Module<>(notifyOnEquipModule, new NoAttributes()));
    }

    private static class TestState {
        public boolean equipCalled;
        public boolean unequipCalled;
    }
}
