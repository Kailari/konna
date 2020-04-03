package fi.jakojaannos.roguelite.game.systems.characters;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;
import fi.jakojaannos.roguelite.game.weapons.WeaponInventory;

public class CharacterAttackSystem implements ECSSystem {

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .requireProvidedResource(Time.class)
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
        final var timeManager = world.getResource(Time.class);

        final var entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            final var input = entityManager.getComponentOf(entity, WeaponInput.class).orElseThrow();
            final var attackAbility = entityManager.getComponentOf(entity, AttackAbility.class)
                                                   .orElseThrow();
            final var inventory = entityManager.getComponentOf(entity, WeaponInventory.class)
                                               .orElseThrow();
            final var equippedSlot = attackAbility.equippedSlot;
            final var equippedWeapon = inventory.getWeaponAtSlot(equippedSlot);

            if (input.attack && !input.previousAttack) {
                equippedWeapon.getWeapon().getTrigger().pull(entityManager, entity, timeManager, equippedWeapon.getState().getTrigger());
            } else if (!input.attack && input.previousAttack) {
                equippedWeapon.getWeapon().getTrigger().release(entityManager, entity, timeManager, equippedWeapon.getState().getTrigger());
            }

            equippedWeapon.getWeapon().fireIfReady(entityManager, entity, timeManager, equippedWeapon, attackAbility);

            input.previousAttack = input.attack;
        });
    }
}
