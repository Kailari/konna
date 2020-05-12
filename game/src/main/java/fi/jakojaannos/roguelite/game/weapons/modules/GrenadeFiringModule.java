package fi.jakojaannos.roguelite.game.weapons.modules;

import org.joml.Vector2d;

import fi.jakojaannos.roguelite.game.data.archetypes.GrenadeArchetype;
import fi.jakojaannos.roguelite.game.data.components.weapon.GrenadeStats;
import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;

public class GrenadeFiringModule implements WeaponModule<GrenadeFiringModule.Attributes> {

    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponFire(this::checkIfCanFire, Phase.CHECK);
        hooks.weaponFire(this::fire, Phase.TRIGGER);
        hooks.weaponFire(this::afterFire, Phase.POST);

        hooks.registerStateFactory(State.class, State::new);
    }

    private void checkIfCanFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        final var timePassed = info.timeManager().getCurrentGameTime() - state.lastShotTimestamp;
        if (timePassed < attributes.timeBetweenShots) {
            event.cancel();
        }
    }

    private void fire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);
        final var attackAbility = info.attackAbility();

        state.velocity.set(attackAbility.targetPosition)
                      .sub(info.shooterTransform().position);
        if (state.velocity.lengthSquared() != 0) {
            state.velocity.normalize(attributes.flightSpeed);
        }

        GrenadeArchetype.createGrenade(info.entities(),
                                       info.shooterTransform().position,
                                       state.velocity,
                                       attackAbility.projectileLayer,
                                       attributes.stats,
                                       info.timeManager().getCurrentGameTime(),
                                       attributes.flightDuration);
    }

    private void afterFire(
            final Weapon weapon,
            final WeaponFireEvent event,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.lastShotTimestamp = info.timeManager().getCurrentGameTime();
    }

    public static class State {
        private final Vector2d velocity = new Vector2d();

        private long lastShotTimestamp;
    }

    public static record Attributes(
            long timeBetweenShots,
            GrenadeStats stats,
            long flightDuration,
            double flightSpeed
    ) {}
}
