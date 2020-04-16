package fi.jakojaannos.roguelite.game.data.events.render;

public record GunshotEvent(Variant variant) {
    public enum Variant {
        SHOTGUN,
        MELEE,
        GATLING,
    }
}
