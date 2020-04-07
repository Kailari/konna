package fi.jakojaannos.roguelite.engine.view.ui.builder;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.AutomaticSizeTag;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.FontSize;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.label.Text;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public class UILabelBuilder extends UIElementBuilder<UILabelBuilder> {
    public UILabelBuilder(
            final UserInterface userInterface,
            final EntityHandle entity,
            final String name,
            final Consumer<Object> componentConsumer
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
