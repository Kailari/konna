package fi.jakojaannos.roguelite.game.weapons.events;

public class ReloadEvent {
    private boolean cancelled;

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }
}
