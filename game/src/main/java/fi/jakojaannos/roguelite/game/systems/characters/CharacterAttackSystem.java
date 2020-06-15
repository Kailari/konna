package fi.jakojaannos.roguelite.game.systems.characters;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponInventory;
import fi.jakojaannos.roguelite.game.weapons.ActionInfo;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;

public class CharacterAttackSystem implements EcsSystem<CharacterAttackSystem.Resources, CharacterAttackSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.forEach(entity -> {
            final var input = entity.getData().input;
            final var attackAbility = entity.getData().attackAbility;
            final var inventory = entity.getData().inventory;
            final var shooterPos = entity.getData().transform;
            final var equippedSlot = attackAbility.equippedSlot;

            final InventoryWeapon equippedWeapon = inventory.slots[equippedSlot];
            final var actionInfo = new ActionInfo(resources.timeManager,
                                                  resources.entities,
                                                  shooterPos,
                                                  attackAbility,
                                                  resources.events.system());

            if (attackAbility.equippedSlot != attackAbility.previousEquippedSlot) {
                inventory.slots[attackAbility.previousEquippedSlot].unequip(actionInfo);
                equippedWeapon.equip(actionInfo);

                attackAbility.previousEquippedSlot = attackAbility.equippedSlot;
            }

            if (input.reload) {
                equippedWeapon.reload(actionInfo);
            }

            if (input.attack && !input.previousAttack) {
                equippedWeapon.pullTrigger(actionInfo);
            } else if (!input.attack && input.previousAttack) {
                equippedWeapon.releaseTrigger(actionInfo);
            }

            equippedWeapon.fireIfReady(actionInfo);

            // HACK: Fixes shotgun reload sounds by forcing a state query each tick. The sound effect check is done
            //       as ugly hack in the stateQuery and after Vulkan renderer we can no longer fire the event on
            //       the render adapter (the event won't propagate over to the next tick like it used to)
            equippedWeapon.doStateQuery(actionInfo);

            input.previousAttack = input.attack;
        });
    }

    public static record EntityData(
            Transform transform,
            WeaponInput input,
            AttackAbility attackAbility,
            WeaponInventory inventory
    ) {}

    public static record Resources(
            TimeManager timeManager,
            Entities entities,
            Events events
    ) {}
}
