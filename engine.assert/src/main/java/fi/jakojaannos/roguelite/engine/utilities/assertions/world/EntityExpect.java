package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

public interface EntityExpect {
    <TComponent> ComponentMatcher<TComponent> toHaveComponent(Class<TComponent> componentClass);

    void toNotHaveComponent(Class<?> componentClass);
}
