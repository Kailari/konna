package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.math.CoordinateHelper;
import fi.jakojaannos.roguelite.game.data.archetypes.BasicProjectileArchetype;
import fi.jakojaannos.roguelite.game.data.components.*;
import lombok.val;
import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

public class CharacterAttackSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .withComponent(Transform.class)
                    .withComponent(CharacterInput.class)
                    .withComponent(CharacterStats.class)
                    .withComponent(CharacterAbilities.class)
                    .withComponent(BasicWeaponStats.class);
    }

    private final Vector2d tmpSpreadOffset = new Vector2d();
    private final Random random = new Random(1337);

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();

        val entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            val input = entityManager.getComponentOf(entity, CharacterInput.class).orElseThrow();
            val abilities = entityManager.getComponentOf(entity, CharacterAbilities.class).orElseThrow();
            val weapon = entityManager.getComponentOf(entity, BasicWeaponStats.class).orElseThrow();

            if (input.attack && abilities.attackTimer >= 1.0 / weapon.attackRate) {
                val characterTransform = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
                val characterStats = entityManager.getComponentOf(entity, CharacterStats.class).orElseThrow();

                val weaponOffset = CoordinateHelper.transformCoordinate(0,
                                                                        0,
                                                                        characterTransform.rotation,
                                                                        characterStats.weaponOffset.x,
                                                                        characterStats.weaponOffset.y,
                                                                        new Vector2d());
                val projectileX = characterTransform.position.x + weaponOffset.x;
                val projectileY = characterTransform.position.y + weaponOffset.y;
                val direction = new Vector2d(abilities.attackTarget)
                        .sub(projectileX, projectileY)
                        .normalize();
                tmpSpreadOffset.set(direction)
                               .perpendicular()
                               .mul((random.nextDouble() * 2.0 - 1.0) * weapon.attackSpread);

                val noise = (random.nextDouble() * 2.0 - 1.0) * weapon.attackProjectileSpeedNoise;
                BasicProjectileArchetype.create(world, projectileX, projectileY, direction, weapon.attackProjectileSpeed + noise, tmpSpreadOffset);


                abilities.attackTimer = 0.0;
            }
            abilities.attackTimer += delta;
        });
    }
}
