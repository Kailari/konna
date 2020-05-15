package fi.jakojaannos.konna.util;

public record BitMask<T extends BitFlags>(int mask) {
    @SafeVarargs
    public static <T extends BitFlags> BitMask<T> bitMask(final T... flags) {
        var mask = 0;
        for (final var flag : flags) {
            mask |= flag.getMask();
        }
        return new BitMask<>(mask);
    }

    public boolean matches(final int flags) {
        return (flags & this.mask) == this.mask;
    }

    public boolean hasBit(final T flag) {
        return (flag.getMask() & this.mask) != 0;
    }
}
