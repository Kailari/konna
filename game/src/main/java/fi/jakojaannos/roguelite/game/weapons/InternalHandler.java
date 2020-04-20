package fi.jakojaannos.roguelite.game.weapons;

public class InternalHandler<TEvent> {
    private final Phase phase;
    private final WeaponEventHandler<TEvent> handler;

    public Phase getPhase() {
        return this.phase;
    }

    public InternalHandler(
            final Phase phase,
            final WeaponEventHandler<TEvent> handler
    ) {
        this.phase = phase;
        this.handler = handler;
    }

    public void handle(
            final Weapon weapon,
            final TEvent event,
            final ActionInfo info
    ) {
        this.handler.handle(weapon, event, info);
    }
}
