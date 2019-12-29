package fi.jakojaannos.roguelite.engine.ui.builder;

import fi.jakojaannos.roguelite.engine.data.components.internal.ui.*;
import fi.jakojaannos.roguelite.engine.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.ui.UIElementType;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class UIElementBuilder<TBuilder extends UIElementBuilder<TBuilder>> {
    private final UIBuilder uiBuilder;
    private final Entity entity;
    protected final Consumer<Component> componentConsumer;

    public UIElementBuilder(
            final UIBuilder uiBuilder,
            final Entity entity,
            final String name,
            final Consumer<Component> componentConsumer
    ) {
        this.uiBuilder = uiBuilder;
        this.entity = entity;
        this.componentConsumer = componentConsumer;
        this.componentConsumer.accept(new Name(name));
        this.componentConsumer.accept(new ElementBoundaries());
    }

    public TBuilder left(final ProportionValue value) {
        this.componentConsumer.accept(new BoundLeft(value));
        return (TBuilder) this;
    }

    public TBuilder width(final ProportionValue value) {
        this.componentConsumer.accept(new BoundWidth(value));
        return (TBuilder) this;
    }

    public TBuilder anchorX(final ProportionValue value) {
        this.componentConsumer.accept(new BoundAnchorX(value));
        return (TBuilder) this;
    }

    public TBuilder anchorY(final ProportionValue value) {
        this.componentConsumer.accept(new BoundAnchorY(value));
        return (TBuilder) this;
    }

    public TBuilder height(final ProportionValue value) {
        this.componentConsumer.accept(new BoundHeight(value));
        return (TBuilder) this;
    }

    public TBuilder top(final ProportionValue value) {
        this.componentConsumer.accept(new BoundTop(value));
        return (TBuilder) this;
    }

    public <TChildElement extends UIElementType<TChildBuilder>, TChildBuilder extends UIElementBuilder<TChildBuilder>>
    TBuilder child(
            final String name,
            final TChildElement childType,
            final Consumer<TChildBuilder> builderConsumer
    ) {
        this.uiBuilder.element(name, childType, builderConsumer.andThen(builder -> builder.componentConsumer.accept(new Parent(this.entity))));
        return (TBuilder) this;
    }
}
