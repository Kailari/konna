package fi.jakojaannos.roguelite.game.data;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DamageInstance {
    public final double damage;
    public final DamageSource<?> source;
}
