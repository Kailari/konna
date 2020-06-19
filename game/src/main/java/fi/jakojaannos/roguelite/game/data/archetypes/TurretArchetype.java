package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.ecs.resources.Entities;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.TurretTag;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.LookAtTargetTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponInventory;
import fi.jakojaannos.roguelite.game.data.resources.Weapons;

public class TurretArchetype {
    public static EntityHandle create(
            final Entities entities,
            final TimeManager timeManager,
            final Transform transform
    ) {
        return entities.createEntity(
                new Transform(transform),
                new SpriteInfo("sprites/turret"),
                new Collider(CollisionLayer.NONE, 1.0, 1.0, 0.5, 0.5),
                new LookAtTargetTag(),
                new TurretTag(),
                new AttackAI(EnemyTag.class, 15.0),
                new WeaponInput(),
                new AttackAbility(DamageSource.Generic.UNDEFINED,
                                  CollisionLayer.PLAYER_PROJECTILE,
                                  0.375,
                                  -1.0,
                                  0),
                new WeaponInventory(Weapons.TURRET_GATLING));
    }
}
