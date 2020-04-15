package fi.jakojaannos.roguelite.game.weapons;

public interface WeaponEventHandler<TState, TAttributes, TEvent> {
    void handle(
            TState state,
            TAttributes attributes,
            TEvent event,
            ActionInfo actionInfo
    );
}
