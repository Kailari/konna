package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import java.util.function.Consumer;

/**
 * Perform expectation assertions on state of a component.
 *
 * @param <TComponent> type of the component being matched
 */
public interface ComponentMatcher<TComponent> {
    /**
     * Expects the given expectation to not throw an assertion error upon execution.
     *
     * @param expectation expectation to perform
     */
    void which(Consumer<TComponent> expectation);

    /**
     * Alias for {@link #which(Consumer)}
     *
     * @param expectation expectation to perform
     */
    default void that(final Consumer<TComponent> expectation) {
        which(expectation);
    }
}
