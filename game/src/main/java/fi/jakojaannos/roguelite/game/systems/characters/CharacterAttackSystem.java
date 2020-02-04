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
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.utilities.math.CoordinateHelper;
import fi.jakojaannos.roguelite.game.data.archetypes.ProjectileArchetype;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class CharacterAttackSystem implements ECSSystem {
    private final Vector2d tmpSpreadOffset = new Vector2d();
    private final Vector2d tmpProjectilePos = new Vector2d();
    private final Vector2d tmpDirection = new Vector2d();

    private final Random random = new Random(1337);

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
            final var attackAbility = entityManager.getComponentOf(entity, AttackAbility.class).orElseThrow();
            final var weapon = entityManager.getComponentOf(entity, WeaponStats.class).orElseThrow();

            if (input.attack && isReadyToAttack(timeManager, attackAbility, weapon)) {
                final var characterTransform = entityManager.getComponentOf(entity, Transform.class)
                                                            .orElseThrow();

                final var weaponOffset = CoordinateHelper.transformCoordinate(0,
                                                                              0,
                                                                              characterTransform.rotation,
                                                                              attackAbility.weaponOffset.x,
                                                                              attackAbility.weaponOffset.y,
                                                                              new Vector2d());
                final var projectilePos = this.tmpProjectilePos.set(characterTransform.position)
                                                               .add(weaponOffset);
                final var direction = attackAbility.targetPosition.sub(projectilePos, this.tmpDirection);
                if (direction.lengthSquared() == 0) {
                    direction.set(1.0, 0.0);
                } else {
                    direction.normalize();
                }

                final var spreadAmount = (this.random.nextDouble() * 2.0 - 1.0) * weapon.attackSpread;
                final var spreadOffset = this.tmpSpreadOffset.set(direction)
                                                             .perpendicular()
                                                             .mul(spreadAmount);

                final var speedNoise = (this.random.nextDouble() * 2.0 - 1.0) * weapon.projectileSpeedNoise;
                final var actualSpeed = weapon.projectileSpeed + speedNoise;

                final var timestamp = timeManager.getCurrentGameTime();
                ProjectileArchetype.create(entityManager,
                                           projectilePos,
                                           direction.normalize(actualSpeed)
                                                    .add(spreadOffset),
                                           attackAbility.damageSource,
                                           attackAbility.projectileLayer,
                                           timestamp,
                                           weapon.projectileLifetimeInTicks);

                attackAbility.lastAttackTimestamp = timestamp;
            }
        });
    }

    private boolean isReadyToAttack(
            final TimeManager timeManager,
            final AttackAbility attack,
            final WeaponStats weapon
    ) {
        final var timeSinceLastAttack = timeManager.getCurrentGameTime() - attack.lastAttackTimestamp;
        final var timeBetweenShots = timeManager.convertToTicks(1.0 / weapon.attackRate);
        return timeSinceLastAttack >= timeBetweenShots;
    }
}
