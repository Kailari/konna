package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.Test;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.systems.characters.CharacterAttackSystem;
import fi.jakojaannos.roguelite.game.weapons.ActionInfo;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponInventory;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CharacterAttackSystemTest {
    private WeaponInput weaponInput;
    private AttackAbility attackAbility;
    private InventoryWeapon slot0;
    private InventoryWeapon slot1;
    private InventoryWeapon slot2;

    void beforeEach(final World world) {
        this.weaponInput = new WeaponInput();
        this.weaponInput.attack = false;
        final var wepInv = new WeaponInventory(3);
        final var entity = world.createEntity(new Transform(0.0, 0.0),
                                              new Velocity(),
                                              weaponInput,
                                              wepInv);
        this.attackAbility = new AttackAbility(
                new DamageSource.Entity(entity),
                CollisionLayer.PLAYER,
                0.0,
                0.0);
        entity.addComponent(attackAbility);

        slot0 = mock(InventoryWeapon.class);
        slot1 = mock(InventoryWeapon.class);
        slot2 = mock(InventoryWeapon.class);

        wepInv.slots[0] = slot0;
        wepInv.slots[1] = slot1;
        wepInv.slots[2] = slot2;
    }

    @Test
    void reloadIsCalledWhenInputIsTrue() {
        whenGame().withSystems(new CharacterAttackSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      weaponInput.reload = true;
                  })
                  .runsSingleTick()
                  .expect(state -> verify(slot0).reload(any(ActionInfo.class)));
    }

    @Test
    void reloadIsNotCalledWhenInputIsFalse() {
        whenGame().withSystems(new CharacterAttackSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      weaponInput.reload = false;
                  })
                  .runsSingleTick()
                  .expect(state -> verify(slot0, never()).reload(any(ActionInfo.class)));
    }

    @Test
    void fireIfReadyIsCalled() {
        whenGame().withSystems(new CharacterAttackSystem())
                  .withState(this::beforeEach)
                  .runsForTicks(30)
                  .expect(state -> verify(slot0, times(30)).fireIfReady(any(ActionInfo.class)));
    }

    @Test
    void triggerPullIsCalledOnceWhenTriggerIsPressed() {
        whenGame().withSystems(new CharacterAttackSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      weaponInput.attack = false;
                  })
                  .runsSingleTick()
                  .expect(state -> verify(slot0, never()).pullTrigger(any(ActionInfo.class)))
                  .expect(state -> weaponInput.attack = true)
                  .runsSingleTick()
                  .expect(state -> verify(slot0).pullTrigger(any(ActionInfo.class)))
                  .runsForTicks(30)
                  .expect(state -> verify(slot0).pullTrigger(any(ActionInfo.class)));
    }

    @Test
    void triggerReleaseIsCalledOnceWhenTriggerIsReleased() {
        whenGame().withSystems(new CharacterAttackSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      weaponInput.attack = false;
                      weaponInput.previousAttack = false;
                  })
                  .runsSingleTick()
                  .expect(state -> verify(slot0, never()).releaseTrigger(any(ActionInfo.class)))
                  .expect(state -> weaponInput.attack = true)
                  .runsSingleTick()
                  .expect(state -> weaponInput.attack = false)
                  .runsForTicks(30)
                  .expect(state -> verify(slot0).releaseTrigger(any(ActionInfo.class)));
    }

    @Test
    void previouslyEquippedSlotIsUpdated() {
        whenGame().withSystems(new CharacterAttackSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      attackAbility.previousEquippedSlot = 0;
                      attackAbility.equippedSlot = 2;
                  })
                  .runsSingleTick()
                  .expect(state -> assertEquals(2, attackAbility.previousEquippedSlot))
                  .runsSingleTick()
                  .expect(state -> assertEquals(2, attackAbility.previousEquippedSlot))
                  .expect(state -> attackAbility.equippedSlot = 1)
                  .runsSingleTick()
                  .expect(state -> assertEquals(1, attackAbility.previousEquippedSlot));
    }

    @Test
    void switchingWeaponEquipsNewWeapon() {
        whenGame().withSystems(new CharacterAttackSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      attackAbility.previousEquippedSlot = 1;
                      attackAbility.equippedSlot = 2;
                  })
                  .runsSingleTick()
                  .expect(state -> verify(slot2).equip(any(ActionInfo.class)));
    }

    @Test
    void switchingWeaponDoesntEquipOldWeapon() {
        whenGame().withSystems(new CharacterAttackSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      attackAbility.previousEquippedSlot = 1;
                      attackAbility.equippedSlot = 2;
                  })
                  .runsSingleTick()
                  .expect(state -> verify(slot1, never()).equip(any(ActionInfo.class)));
    }

    @Test
    void switchingWeaponUnequipsPreviousWeapon() {
        whenGame().withSystems(new CharacterAttackSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      attackAbility.previousEquippedSlot = 1;
                      attackAbility.equippedSlot = 2;
                  })
                  .runsSingleTick()
                  .expect(state -> verify(slot1).unequip(any(ActionInfo.class)));
    }

    @Test
    void switchingWeaponDoesntUnequipNewWeapon() {
        whenGame().withSystems(new CharacterAttackSystem())
                  .withState(this::beforeEach)
                  .withState(world -> {
                      attackAbility.previousEquippedSlot = 1;
                      attackAbility.equippedSlot = 2;
                  })
                  .runsSingleTick()
                  .expect(state -> verify(slot2, never()).unequip(any(ActionInfo.class)));
    }
}
