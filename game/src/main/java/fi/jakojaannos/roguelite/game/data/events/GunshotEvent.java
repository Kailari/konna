package fi.jakojaannos.roguelite.game.data.events;

public record GunshotEvent(Variant variant) {
    public enum Variant {
        RIFLE,
        MELEE,
        GATLING,
        SHOTGUN,
        CLICK,
        PUMP,
        SHOTGUN_RELOAD,
    }
}
