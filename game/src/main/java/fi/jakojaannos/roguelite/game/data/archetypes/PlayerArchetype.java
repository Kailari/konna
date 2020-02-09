package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.*;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class PlayerArchetype {
    public static Entity create(
            final EntityManager entityManager,
            final TimeManager timeManager,
            final Transform transform
    ) {
        final var player = entityManager.createEntity();
        entityManager.addComponentTo(player, transform);
        entityManager.addComponentTo(player, new Velocity());
        entityManager.addComponentTo(player, Physics.builder().friction(42.0 * 2).build());
        entityManager.addComponentTo(player, new MovementInput());
        entityManager.addComponentTo(player, new WeaponInput());
        entityManager.addComponentTo(player, new AttackAbility(new DamageSource.Entity(player),
                                                               CollisionLayer.PLAYER_PROJECTILE,
                                                               0.25,
                                                               -0.5));
        entityManager.addComponentTo(player, new Collider(CollisionLayer.PLAYER, 1.0, 1.0, 0.5, 0.5));
        entityManager.addComponentTo(player, new PlayerTag());
        entityManager.addComponentTo(player, new LookAtTargetTag());
        entityManager.addComponentTo(player, new WalkingMovementAbility(10.0f, 69.0f * 1.5));
        entityManager.addComponentTo(player, new WeaponStats(timeManager.convertToTicks(1.0 / 2.5),
                                                             80.0,
                                                             2.5,
                                                             4,
                                                             -1,
                                                             10));
        entityManager.addComponentTo(player, new SpriteInfo("sprites/player"));
        entityManager.addComponentTo(player, new Health(10));
        return player;
    }
}
