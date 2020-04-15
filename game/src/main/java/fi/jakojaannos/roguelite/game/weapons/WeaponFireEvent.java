package fi.jakojaannos.roguelite.game.weapons;

public class WeaponFireEvent {
    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
}
