package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.DamageInstance;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.resources.Explosions;

public class ExplosionHandlerSystem implements EcsSystem<ExplosionHandlerSystem.Resources, ExplosionHandlerSystem.EntityData, EcsSystem.NoEvents> {

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.forEach(entity -> resources.explosions.getExplosions().forEach(explosion -> {

            final var entityPos = entity.getData().transform.position;
            if (entityPos.distanceSquared(explosion.location()) > explosion.radiusSquared()) {
                return;
            }
            final var hp = entity.getData().health;
            hp.addDamageInstance(new DamageInstance(explosion.damage(),
                                                    explosion.damageSource()),
                                 resources.timeManager.getCurrentGameTime());

            final var physics = entity.getData().physics;
            final var pushForce = new Vector2d(entityPos).sub(explosion.location());
            if (pushForce.lengthSquared() != 0) {
                pushForce.normalize(explosion.pushForce());
                physics.applyForce(pushForce);
            }
        }));
    }

    public static record EntityData(
            Transform transform,
            Health health,
            Physics physics
    ) {}

    public static record Resources(
            Explosions explosions,
            TimeManager timeManager
    ) {}
}
