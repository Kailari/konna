package fi.jakojaannos.roguelite.engine.view.ui;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.AutomaticSizeTag;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.FontSize;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.label.Text;
import fi.jakojaannos.roguelite.engine.view.text.Font;
import fi.jakojaannos.roguelite.engine.view.ui.builder.UIBuilder;

import java.util.function.Consumer;

public class UILabelBuilder extends UIElementBuilder<UILabelBuilder> {
    public UILabelBuilder(
            final UIBuilder uiBuilder,
            final Entity entity,
            final String name,
            final Consumer<Component> componentConsumer
    ) {
        super(uiBuilder, entity, name, componentConsumer);
        this.componentConsumer.accept(new AutomaticSizeTag());
    }

    public UILabelBuilder text(final String text, final Font font) {
        this.componentConsumer.accept(new Text(text, font));
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
