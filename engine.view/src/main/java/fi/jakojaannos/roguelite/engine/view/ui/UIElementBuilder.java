package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.*;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class UIElementBuilder<TBuilder extends UIElementBuilder<TBuilder>> {
    protected final Consumer<Component> componentConsumer;

    public UIElementBuilder(
            final String name,
            final Consumer<Component> componentConsumer
    ) {
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

    public TBuilder height(final ProportionValue value) {
        this.componentConsumer.accept(new BoundHeight(value));
        return (TBuilder) this;
    }

    public TBuilder top(final ProportionValue value) {
        this.componentConsumer.accept(new BoundTop(value));
        return (TBuilder) this;
    }
}
