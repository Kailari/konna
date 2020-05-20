package fi.jakojaannos.roguelite.game.data.components.weapon;

import fi.jakojaannos.roguelite.game.data.DamageSource;

public class Fuse {
    public long fuseStart;
    public long fuseTime;

    public DamageSource<?> damageSource;

    public Fuse(final long fuseStart, final long fuseTime, final DamageSource<?> damageSource) {
        this.fuseStart = fuseStart;
        this.fuseTime = fuseTime;
        this.damageSource = damageSource;
    }

    public Fuse(final long fuseStart, final long fuseTime) {
        this(fuseStart, fuseTime, DamageSource.Generic.UNDEFINED);
    }
}
