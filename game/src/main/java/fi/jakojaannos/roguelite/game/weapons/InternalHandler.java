package fi.jakojaannos.roguelite.game.weapons;

public class InternalHandler<TState, TAttributes, TEvent> {
    private final WeaponModule<TState, TAttributes> weaponModule;
    private final Phase phase;
    private final WeaponEventHandler<TState, TAttributes, TEvent> handler;

    public Phase getPhase() {
        return this.phase;
    }

    public InternalHandler(
            final WeaponModule<TState, TAttributes> weaponModule,
            final Phase phase,
            final WeaponEventHandler<TState, TAttributes, TEvent> handler
    ) {
        this.weaponModule = weaponModule;
        this.phase = phase;
        this.handler = handler;
    }

    public void handle(
            final InventoryWeapon weapon,
            final TEvent event,
            final ActionInfo info
    ) {
        final var attributes = this.weaponModule.getAttributes(weapon.getAttributes());
        final var state = this.weaponModule.getState(weapon.getState(), attributes);
        this.handler.handle(state, attributes, event, info);
    }
}
