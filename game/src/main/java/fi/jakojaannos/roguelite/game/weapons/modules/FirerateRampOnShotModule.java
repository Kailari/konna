package fi.jakojaannos.roguelite.game.weapons.modules;

import fi.jakojaannos.roguelite.game.weapons.*;
import fi.jakojaannos.roguelite.game.weapons.events.TriggerReleaseEvent;
import fi.jakojaannos.roguelite.game.weapons.events.WeaponFireEvent;

public class FirerateRampOnShotModule implements WeaponModule<FirerateRampOnShotModule.Attributes> {
    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponFire(this::afterShot, Phase.POST);
        hooks.triggerRelease(this::releaseTrigger, Phase.POST);
    }

    private void afterShot(final Weapon weapon, final WeaponFireEvent weaponFireEvent, final ActionInfo actionInfo) {
        final var state = weapon.getState(ProjectileFiringModule.State.class);
        final var attributes = weapon.getAttributes(Attributes.class);

        state.firerateModifier = Math.min(state.firerateModifier + attributes.amountPerShot, attributes.max);
    }

    private void releaseTrigger(
            final Weapon weapon,
            final TriggerReleaseEvent triggerReleaseEvent,
            final ActionInfo actionInfo
    ) {
        final var state = weapon.getState(ProjectileFiringModule.State.class);
        state.firerateModifier = 0.0f;
    }

    public static record Attributes(
            double amountPerShot,
            double max
    ) {}
}
