package fi.jakojaannos.roguelite.engine.utilities.assertions.world.entity;

import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.ComponentMatcher;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.EntityExpect;

import static org.junit.jupiter.api.Assertions.fail;

public record EntityExpectImpl(EntityHandle entity) implements EntityExpect {
    @Override
    public <TComponent> ComponentMatcher<TComponent> toHaveComponent(
            final Class<TComponent> componentClass
    ) {
        final var maybeComponent = this.entity.getComponent(componentClass);
        if (maybeComponent.isEmpty()) {
            fail(String.format("Entity did not have the expected component \"%s\"!",
                               componentClass.getSimpleName()));
        }

        return new ComponentMatcherImpl<>(maybeComponent.get());
    }

    @Override
    public void toNotHaveComponent(final Class<?> componentClass) {
        this.entity.getComponent(componentClass)
                   .ifPresent(o -> fail(String.format("Expected the entity to not have component \"%s\"!",
                                                      componentClass.getSimpleName())));
    }
}
