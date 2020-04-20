package fi.jakojaannos.roguelite.game.weapons.events;

public class TriggerReleaseEvent {
    private boolean cancelled;

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
