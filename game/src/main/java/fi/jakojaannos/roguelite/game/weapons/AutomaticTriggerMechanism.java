package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

// TODO: Provide callbacks for reacting to events like "reload" or "weapon fired"

public class AutomaticTriggerMechanism implements Weapon.TriggerMechanism {
    private boolean triggerDown;

    @Override
    public void pull(
            final TimeManager timeManager,
            final AttackAbility attackAbility,
            final WeaponStats weaponStats
    ) {
        this.triggerDown = true;
    }

    @Override
    public void release(
            final TimeManager timeManager,
            final AttackAbility attackAbility,
            final WeaponStats weaponStats
    ) {
        this.triggerDown = false;
    }

    @Override
    public boolean shouldTrigger(
            final TimeManager timeManager,
            final AttackAbility attackAbility,
            final WeaponStats weaponStats
    ) {
        return this.triggerDown;
    }
}
