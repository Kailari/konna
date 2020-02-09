package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.LookAtTargetTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class TurretArchetype {
    public static Entity create(
            final EntityManager entityManager,
            final TimeManager timeManager,
            final Transform transform
    ) {
        final var turret = entityManager.createEntity();
        entityManager.addComponentTo(turret, transform);
        entityManager.addComponentTo(turret, new SpriteInfo("sprites/turret"));
        entityManager.addComponentTo(turret, new Collider(CollisionLayer.NONE, 1.0, 1.0, 0.5, 0.5));
        entityManager.addComponentTo(turret, new LookAtTargetTag());

        entityManager.addComponentTo(turret, new AttackAI(EnemyTag.class, 15.0));
        entityManager.addComponentTo(turret, new WeaponInput());
        entityManager.addComponentTo(turret, new AttackAbility(DamageSource.Generic.UNDEFINED,
                                                               CollisionLayer.PLAYER_PROJECTILE,
                                                               0.375,
                                                               -1.0));
        entityManager.addComponentTo(turret, new WeaponStats(timeManager.convertToTicks(1.0 / 3.0),
                                                             50.0,
                                                             5.0));

        return turret;
    }
}
