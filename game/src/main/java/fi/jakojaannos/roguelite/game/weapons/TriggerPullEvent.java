package fi.jakojaannos.roguelite.game.weapons;

public class TriggerPullEvent {
    private boolean cancelled;

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
