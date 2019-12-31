package fi.jakojaannos.roguelite.engine.ui.builder;

import fi.jakojaannos.roguelite.engine.data.components.internal.ui.AutomaticSizeTag;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.FontSize;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.label.Text;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;

import java.util.function.Consumer;

public class UILabelBuilder extends UIElementBuilder<UILabelBuilder> {
    public UILabelBuilder(
            final UserInterface userInterface,
            final Entity entity,
            final String name,
            final Consumer<Component> componentConsumer
    ) {
        super(userInterface, entity, name, componentConsumer);
        this.componentConsumer.accept(new AutomaticSizeTag());
    }

    public UILabelBuilder text(final String text) {
        this.componentConsumer.accept(new Text(text));
        return this;
    }

    public UILabelBuilder fontSize(final int value) {
        this.componentConsumer.accept(new FontSize(value));
        return this;
    }

    @Override
    public UILabelBuilder width(final ProportionValue value) {
        throw new UnsupportedOperationException("Cannot set width of a label. Use fontSize instead.");
    }

    @Override
    public UILabelBuilder height(final ProportionValue value) {
        throw new UnsupportedOperationException("Cannot set height of a label. Use fontSize instead.");
    }
}
