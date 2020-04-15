package fi.jakojaannos.roguelite.game.systems.characters;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;
import fi.jakojaannos.roguelite.game.weapons.ActionInfo;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;
import fi.jakojaannos.roguelite.game.weapons.WeaponInventory;

public class CharacterAttackSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .requireProvidedResource(TimeManager.class)
                    .withComponent(Transform.class)
                    .withComponent(WeaponInput.class)
                    .withComponent(AttackAbility.class)
                    .withComponent(WeaponInventory.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var timeManager = world.fetchResource(TimeManager.class);

        final var entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            final var input = entityManager.getComponentOf(entity, WeaponInput.class).orElseThrow();
            final var attackAbility = entityManager.getComponentOf(entity, AttackAbility.class)
                                                   .orElseThrow();
            final var inventory = entityManager.getComponentOf(entity, WeaponInventory.class)
                                               .orElseThrow();
            final var shooterPos = entityManager.getComponentOf(entity, Transform.class)
                                                .orElseThrow();
            final var equippedSlot = attackAbility.equippedSlot;
            final InventoryWeapon equippedWeapon = inventory.getWeaponAtSlot(equippedSlot);
            final var actionInfo = new ActionInfo(timeManager, entityManager, shooterPos, attackAbility);

            if (input.reload) {
                equippedWeapon.reload(actionInfo);
            }

            if (input.attack && !input.previousAttack) {
                equippedWeapon.pullTrigger(actionInfo);
            } else if (!input.attack && input.previousAttack) {
                equippedWeapon.releaseTrigger(actionInfo);
            }

            equippedWeapon.fireIfReady(actionInfo);

            input.previousAttack = input.attack;
        });
    }
}
