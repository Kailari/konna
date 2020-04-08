package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import java.util.function.Consumer;

public interface ComponentMatcher<TComponent> {
    void which(Consumer<TComponent> expectation);

    default void that(final Consumer<TComponent> expectation) {
        which(expectation);
    }
}
