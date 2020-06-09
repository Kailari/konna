package fi.jakojaannos.roguelite.engine.utilities.assertions.world.entity;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.utilities.assertions.world.ComponentMatcher;

public record ComponentMatcherImpl<TComponent>(
        TComponent component
) implements ComponentMatcher<TComponent> {
    @Override
    public void which(final Consumer<TComponent> expectation) {
        expectation.accept(this.component);
    }
}
