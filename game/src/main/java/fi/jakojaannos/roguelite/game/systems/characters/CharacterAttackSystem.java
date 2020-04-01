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
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class CharacterAttackSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .requireProvidedResource(Time.class)
                    .withComponent(Transform.class)
                    .withComponent(WeaponInput.class)
                    .withComponent(AttackAbility.class)
                    .withComponent(WeaponStats.class);
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
            final var weaponStats = entityManager.getComponentOf(entity, WeaponStats.class)
                                                 .orElseThrow();
            final var attackAbility = entityManager.getComponentOf(entity, AttackAbility.class)
                                                   .orElseThrow();
            final var weapon = attackAbility.equippedWeapon;

            if (input.attack && !input.previousAttack) {
                weapon.getTrigger().pull(entityManager, entity, timeManager, attackAbility, weaponStats);
            } else if (!input.attack && input.previousAttack) {
                weapon.getTrigger().release(entityManager, entity, timeManager, attackAbility, weaponStats);
            }

            weapon.fireIfReady(entityManager, attackAbility, weaponStats, timeManager, entity);

            input.previousAttack = input.attack;
        });
    }
}
