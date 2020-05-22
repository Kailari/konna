package fi.jakojaannos.konna.engine.view.ui;

public interface UiUnit {
    /**
     * Creates new unit value with the given absolute pixel size.
     *
     * @param value value in pixels
     *
     * @return the value
     */
    static UiUnit pixels(final double value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates new unit value described as percentage of parent. E.g. for widths, this is percentage of width of the
     * parent, percentage of parent heights for height values etc.
     * <p>
     * Values should be written as <i>percentages</i> e.g. 100% is written as 100, not 1.0. If you wish to use
     * multiples, use {@link #multiple(double)}.
     *
     * @param value value as percentage of respective parent value. Usually value within range 0..100.
     *
     * @return the value
     */
    static UiUnit percent(final double value) {
        return multiple(value / 100.0);
    }

    /**
     * Same as {@link #percent(double)}, but the values are described as percentage multipliers; e.g. 100% is 1.0 and
     * 50% is 0.5.
     *
     * @param value value as percentage multiplier of respective parent value. Usually value within range 0..1
     *
     * @return the value
     */
    static UiUnit multiple(final double value) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
