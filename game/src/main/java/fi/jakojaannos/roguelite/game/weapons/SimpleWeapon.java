package fi.jakojaannos.roguelite.game.weapons;

public class SimpleWeapon implements Weapon {
    private final Weapon.TriggerMechanism triggerMechanism;
    private final FiringMechanism firingMechanism;

    public SimpleWeapon() {
        this.triggerMechanism = new AutomaticTriggerMechanism();
        this.firingMechanism = new ProjectileFiringMechanism();
    }

    @Override
    public TriggerMechanism getTrigger() {
        return this.triggerMechanism;
    }

    @Override
    public FiringMechanism getFiringMechanism() {
        return this.firingMechanism;
    }
}
