package fi.jakojaannos.roguelite.game.data.components.weapon;

public class Fuse {
    public long fuseStart;
    public long fuseTime;

    public Fuse(final long fuseStart, final long fuseTime) {
        this.fuseStart = fuseStart;
        this.fuseTime = fuseTime;
    }
}
