package fi.jakojaannos.roguelite.game.systems.characters;

import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.math.CoordinateHelper;
import fi.jakojaannos.roguelite.game.data.archetypes.BasicProjectileArchetype;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.BasicWeaponStats;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class CharacterAttackSystem implements ECSSystem {
    private final Vector2d tmpSpreadOffset = new Vector2d();
    private final Random random = new Random(1337);

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .withComponent(Transform.class)
                    .withComponent(CharacterInput.class)
                    .withComponent(WalkingMovementAbility.class)
                    .withComponent(CharacterAbilities.class)
                    .withComponent(BasicWeaponStats.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var delta = world.getResource(Time.class).getTimeStepInSeconds();

        final var entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            final var input = entityManager.getComponentOf(entity, CharacterInput.class)
                                           .orElseThrow();
            final var abilities = entityManager.getComponentOf(entity, CharacterAbilities.class)
                                               .orElseThrow();
            final var weapon = entityManager.getComponentOf(entity, BasicWeaponStats.class)
                                            .orElseThrow();

            if (input.attack && abilities.attackTimer >= 1.0 / weapon.attackRate) {
                final var characterTransform = entityManager.getComponentOf(entity, Transform.class)
                                                            .orElseThrow();
                final var characterStats = entityManager.getComponentOf(entity, WalkingMovementAbility.class)
                                                        .orElseThrow();

                final var weaponOffset = CoordinateHelper.transformCoordinate(
                        0,
                        0,
                        characterTransform.rotation,
                        characterStats.weaponOffset.x,
                        characterStats.weaponOffset.y,
                        new Vector2d()
                );
                final var projectileX = characterTransform.position.x + weaponOffset.x;
                final var projectileY = characterTransform.position.y + weaponOffset.y;
                final var direction = new Vector2d(abilities.attackTarget).sub(projectileX, projectileY)
                                                                          .normalize();
                this.tmpSpreadOffset.set(direction)
                                    .perpendicular()
                                    .mul((this.random.nextDouble() * 2.0 - 1.0) * weapon.attackSpread);

                final var noise = (this.random.nextDouble() * 2.0 - 1.0) * weapon.attackProjectileSpeedNoise;
                BasicProjectileArchetype.create(abilities.damageSource,
                                                world,
                                                projectileX,
                                                projectileY,
                                                direction,
                                                weapon.attackProjectileSpeed + noise,
                                                this.tmpSpreadOffset);

                abilities.attackTimer = 0.0;
            }

            abilities.attackTimer += delta;
        });
    }
}
