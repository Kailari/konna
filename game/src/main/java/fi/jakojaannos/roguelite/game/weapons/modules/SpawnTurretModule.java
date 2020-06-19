package fi.jakojaannos.roguelite.game.weapons.modules;

import org.joml.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.archetypes.TurretArchetype;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;

public class SpawnTurretModule implements WeaponModule<SpawnTurretModule.Attributes> {
    private static final Logger LOG = LoggerFactory.getLogger(SpawnTurretModule.class);

    @Override
    public void register(
            final WeaponHooks hooks, final Attributes attributes
    ) {
        hooks.weaponFire(this::weaponFire, Phase.TRIGGER);

        hooks.registerStateFactory(State.class, State::new);
    }

    private void weaponFire(final Weapon weapon, final WeaponFireEvent event, final ActionInfo info) {
        final var attributes = weapon.getAttributes(Attributes.class);
        final var distSq = attributes.maxPlacingDistance * attributes.maxPlacingDistance;

        final var shooterPos = info.shooterTransform().position;
        final var aimPos = info.attackAbility().targetPosition;
        final var targetPos = new Vector2d(aimPos);

        if (targetPos.distanceSquared(shooterPos) > distSq) {
            // if shooter is aiming too far, find a position in the same direction but closer to shooter

            targetPos.sub(shooterPos);
            // I think it's impossible for this to be zero, but I'd rather have an error msg than hard-to-trace NaN bug
            if (targetPos.lengthSquared() == 0) {
                LOG.error("Error when placing a turret, cannot normalize vector! Shooter pos: {}, aiming at: {}, max placing dist^2: {}",
                          shooterPos, aimPos, distSq);
                return;
            }
            targetPos.normalize(attributes.maxPlacingDistance)
                     .add(shooterPos);
        }

        TurretArchetype.create(info.entities(),
                               info.timeManager(),
                               new Transform(targetPos));
    }

    public static class State {
    }

    public static record Attributes(double maxPlacingDistance) {}
}
