package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerEnemyAI;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.val;

public class FollowerArchetype {
    public static Entity spawnFollower(
            final EntityManager entityManager,
            final Transform spawnerTransform,
            final SpawnerComponent spawnerComponent
    ) {
        return create(entityManager, new Transform(spawnerTransform));
    }


    public static Entity create(
            final EntityManager entityManager,
            final Transform transform
    ) {
        val follower = entityManager.createEntity();
        entityManager.addComponentTo(follower, transform);
        entityManager.addComponentTo(follower, new Velocity());
        entityManager.addComponentTo(follower, createPhysics());
        entityManager.addComponentTo(follower, new CharacterInput());
        entityManager.addComponentTo(follower, new Health(3));
        entityManager.addComponentTo(follower, new Collider(CollisionLayer.ENEMY, 1.0, 1.0, 0.5, 0.5));
        entityManager.addComponentTo(follower, createMovementStats());
        entityManager.addComponentTo(follower, createEnemyAI());
        entityManager.addComponentTo(follower, createSpriteInfo());
        entityManager.addComponentTo(follower, new EnemyTag());
        entityManager.addComponentTo(follower, new CharacterAbilities(new DamageSource.Entity(follower)));
        entityManager.addComponentTo(follower, new EnemyMeleeWeaponStats());

        return follower;
    }

    private static Physics createPhysics() {
        final var physics = new Physics();
        physics.friction = 35.0;
        return physics;
    }


    private static WalkingMovementAbility createMovementStats() {
        return new WalkingMovementAbility(
                4.0,
                50.0
        );
    }

    private static FollowerEnemyAI createEnemyAI() {
        return new FollowerEnemyAI(25.0f, 1.0f);
    }


    private static SpriteInfo createSpriteInfo() {
        val sprite = new SpriteInfo();
        sprite.spriteName = "sprites/enemy";

        return sprite;
    }
}
