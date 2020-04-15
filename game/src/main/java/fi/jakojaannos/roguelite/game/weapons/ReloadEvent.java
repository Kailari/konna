package fi.jakojaannos.roguelite.game.weapons;

public class ReloadEvent {
    private boolean cancelled;
    private boolean shouldFire;

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void tryShoot() {
        this.shouldFire = true;
    }

    public boolean shouldFire() {
        return this.shouldFire;
    }
}
