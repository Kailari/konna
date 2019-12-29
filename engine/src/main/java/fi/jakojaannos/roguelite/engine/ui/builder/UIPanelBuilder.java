package fi.jakojaannos.roguelite.engine.ui.builder;

import fi.jakojaannos.roguelite.engine.data.components.internal.ui.panel.BorderSize;
import fi.jakojaannos.roguelite.engine.data.components.internal.ui.panel.PanelSprite;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;

import java.util.function.Consumer;

public final class UIPanelBuilder extends UIElementBuilder<UIPanelBuilder> {
    public UIPanelBuilder(
            final UIBuilder uiBuilder,
            final Entity entity,
            final String name,
            final Consumer<Component> componentConsumer
    ) {
        super(uiBuilder, entity, name, componentConsumer);
    }

    public UIPanelBuilder borderSize(final int value) {
        this.componentConsumer.accept(new BorderSize(value));
        return this;
    }

    public UIPanelBuilder sprite(final String sprite) {
        this.componentConsumer.accept(new PanelSprite(sprite));
        return this;
    }
}
