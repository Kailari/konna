package fi.jakojaannos.roguelite.game.weapons;

public interface WeaponEventHandler<TEvent> {
    void handle(Weapon weapon, TEvent event, ActionInfo actionInfo);
}
