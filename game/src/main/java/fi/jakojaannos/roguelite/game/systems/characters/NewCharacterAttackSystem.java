package fi.jakojaannos.roguelite.game.systems.characters;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.Requirements;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class NewCharacterAttackSystem implements EcsSystem<NewCharacterAttackSystem.Resources, NewCharacterAttackSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public Requirements<Resources, EntityData, NoEvents> declareRequirements(
            final Requirements.Builder<Resources, EntityData, NoEvents> require
    ) {
        return require.entityData(EntityData.class)
                      .resources(Resources.class)
                      .build();
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var timeManager = resources.time();

        entities.forEach(entityHandle -> {
            final var attackAbility = entityHandle.getData().attackAbility;
            final var input = entityHandle.getData().weaponInput;
            final var weaponStats = entityHandle.getData().weaponStats;

            final var weapon = attackAbility.equippedWeapon;
            if (weapon == null) {
                return;
            }

            if (input.attack && !input.previousAttack) {
                weapon.getTrigger().pull(timeManager, attackAbility, weaponStats);
            } else if (!input.attack && input.previousAttack) {
                weapon.getTrigger().release(timeManager, attackAbility, weaponStats);
            }

            weapon.fireIfReady(entityHandle, attackAbility, weaponStats, timeManager);

            input.previousAttack = input.attack;
        });
    }

    public static record Resources(Time time) {}

    public static record EntityData(
            Transform transform,
            WeaponInput weaponInput,
            AttackAbility attackAbility,
            WeaponStats weaponStats
    ) {}
}
