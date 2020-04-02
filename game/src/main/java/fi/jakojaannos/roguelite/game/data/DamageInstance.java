package fi.jakojaannos.roguelite.game.data;

public record DamageInstance(
        double damage,
        DamageSource<?>source
) {
}
